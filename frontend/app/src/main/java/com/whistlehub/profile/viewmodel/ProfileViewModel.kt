package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.data.repository.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileService: ProfileService,
    val userRepository: UserRepository
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val _memberId = MutableStateFlow(0)
    val memberId: StateFlow<Int> get() = _memberId

    private val _profile = MutableStateFlow<ProfileResponse.GetProfileResponse?>(null)
    val profile: MutableStateFlow<ProfileResponse.GetProfileResponse?> get() = _profile

    private val _tracks =
        MutableStateFlow<List<ProfileResponse.GetMemberTracksResponse>>(emptyList())
    val tracks: StateFlow<List<ProfileResponse.GetMemberTracksResponse>> get() = _tracks

    private val _followers =
        MutableStateFlow<List<ProfileResponse.GetFollowersResponse>>(emptyList())
    val followers: StateFlow<List<ProfileResponse.GetFollowersResponse>> get() = _followers

    private val _followings =
        MutableStateFlow<List<ProfileResponse.GetFollowingsResponse>>(emptyList())
    val followings: StateFlow<List<ProfileResponse.GetFollowingsResponse>> get() = _followings

    private val _hasMoreFollowers = MutableStateFlow(true)
    val hasMoreFollowers: StateFlow<Boolean> get() = _hasMoreFollowers

    private val _hasMoreFollowings = MutableStateFlow(true)
    val hasMoreFollowings: StateFlow<Boolean> get() = _hasMoreFollowings

    private val _followerCount = MutableStateFlow(0)
    val followerCount: StateFlow<Int> get() = _followerCount

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> get() = _followingCount

    private val _trackCount = MutableStateFlow(0)
    val trackCount: StateFlow<Int> get() = _trackCount

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // 검색 결과 상태 추가
    private val _searchResults = MutableStateFlow<List<ProfileResponse.SearchProfileResponse>>(emptyList())
    val searchResults: StateFlow<List<ProfileResponse.SearchProfileResponse>> get() = _searchResults

    private val _searchQueryDebounce = MutableStateFlow("")
    private var searchJob: Job? = null


    private val _myFollowings = MutableStateFlow<List<ProfileResponse.GetFollowingsResponse>>(emptyList())
    val myFollowings: StateFlow<List<ProfileResponse.GetFollowingsResponse>> get() = _myFollowings

    init {
        // 초기화 작업 한 번만 실행
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user != null) {
                _memberId.value = user.memberId
                loadMyFollowings()
                Log.d("ProfileViewModel", "User ID 초기화 완료: ${user.memberId}")
                _isInitialized.value = true
            } else {
                Log.d("ProfileViewModel", "사용자 정보 없음")
            }
        }
    }

    // 모든 함수에서 현재 ID 체크 추가
    private fun ensureValidUserId(): Boolean {
        val currentId = _memberId.value
        if (currentId <= 0) {
            Log.e("ProfileViewModel", "유효하지 않은 사용자 ID: $currentId")
            return false
        }
        return true
    }

    fun loadProfile(targetMemberId: Int) {
        viewModelScope.launch {
            isInitialized.first { it }
            try {
                val profileResponse = profileService.getProfile(targetMemberId)
                if (profileResponse.code == "SU") {
                    profileResponse.payload?.let { profileData ->
                        _profile.emit(profileData)

                        // Update counts directly from the profile response
                        _followerCount.emit(profileData.followerCount)
                        _followingCount.emit(profileData.followingCount)
                        _trackCount.emit(profileData.trackCount)

                        // No need to call loadFollowerCount and loadFollowingCount
                        // Still need to load the follower/following lists for the detailed view
                        loadFollowers(targetMemberId, page = 0, size = 15)
                        loadFollowings(targetMemberId, page = 0, size = 15)
                    }

                    // 프로필 로드 완료 후에만 팔로우 상태 체크
                    if (targetMemberId != _memberId.value) {
                        checkFollowStatus(targetMemberId)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception while loading profile", e)
            }
        }
    }

    fun loadTracks(memberId: Int, page: Int = 0, size: Int = 9) {
        viewModelScope.launch {
            try {
                val tracksResponse = profileService.getMemberTracks(memberId, page, size)
                if (tracksResponse.code == "SU") {
                    // 페이지가 0이면 초기화, 아니면 기존 목록에 추가
                    if (page == 0) {
                        _tracks.emit(tracksResponse.payload ?: emptyList())
                    } else {
                        val currentTracks = _tracks.value.toMutableList()
                        val newTracks = tracksResponse.payload ?: emptyList()

                        // 받은 응답이 요청 size보다 적으면 더 이상 데이터가 없는 것
                        if (newTracks.size < size) {
                            Log.d("ProfileViewModel", "Received fewer tracks than requested, no more pages")
                        }

                        currentTracks.addAll(newTracks)
                        _tracks.emit(currentTracks)
                    }
                } else {
                    _errorMessage.value = tracksResponse.message
                    Log.e("ProfileViewModel", "Failed to load tracks: ${tracksResponse.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
                Log.e("ProfileViewModel", "Exception while loading tracks", e)
            }
        }
    }

    fun loadFollowers(memberId: Int, page: Int = 0, size: Int = 15) {
        viewModelScope.launch {
            try {
                val followersResponse = profileService.getFollowers(memberId, page, size)
                if (followersResponse.code == "SU") {
                    val newFollowers = followersResponse.payload ?: emptyList()

                    // Check if we've reached the end of the list
                    _hasMoreFollowers.emit(newFollowers.size >= size)

                    // Pagination logic
                    if (page == 0) {
                        _followers.emit(newFollowers)
                    } else {
                        val currentFollowers = _followers.value.toMutableList()
                        currentFollowers.addAll(newFollowers)
                        _followers.emit(currentFollowers)
                    }

                    Log.d("ProfileViewModel", "Loaded ${newFollowers.size} followers, hasMore: ${_hasMoreFollowers.value}")
                } else {
                    _errorMessage.value = followersResponse.message
                    Log.e("ProfileViewModel", "Failed to load followers: ${followersResponse.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
                Log.e("ProfileViewModel", "Exception while loading followers", e)
            }
        }
    }

    fun loadFollowings(memberId: Int, page: Int = 0, size: Int = 15) {
        viewModelScope.launch {
            try {
                val followingsResponse = profileService.getFollowings(memberId, page, size)
                if (followingsResponse.code == "SU") {
                    val newFollowings = followingsResponse.payload ?: emptyList()

                    // Check if we've reached the end of the list
                    _hasMoreFollowings.emit(newFollowings.size >= size)

                    // Pagination logic
                    if (page == 0) {
                        _followings.emit(newFollowings)
                    } else {
                        val currentFollowings = _followings.value.toMutableList()
                        currentFollowings.addAll(newFollowings)
                        _followings.emit(currentFollowings)
                    }

                    Log.d("ProfileViewModel", "Loaded ${newFollowings.size} followings, hasMore: ${_hasMoreFollowings.value}")
                } else {
                    _errorMessage.value = followingsResponse.message
                    Log.e("ProfileViewModel", "Failed to load followings: ${followingsResponse.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
                Log.e("ProfileViewModel", "Exception while loading followings", e)
            }
        }
    }

    fun loadMyFollowings(page: Int = 0, size: Int = 999) {
        viewModelScope.launch {
            try {
                val response = profileService.getFollowings(_memberId.value, page, size)
                if (response.code == "SU") {
                    _myFollowings.emit(response.payload ?: emptyList())
                } else {
                    Log.e("ProfileViewModel", "Failed to load my followings: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception while loading my followings", e)
            }
        }
    }

    fun checkFollowStatus(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "팔로우 상태 확인 시작: targetId=$targetMemberId")

                // 팔로잉 목록을 새로 로드하여 최신 상태 확인
                val followingsResponse = profileService.getFollowings(_memberId.value, 0, 999)

                if (followingsResponse.code == "SU") {
                    val followingsList = followingsResponse.payload ?: emptyList()

                    // 캐시 업데이트
                    _myFollowings.value = followingsList

                    // 팔로우 상태 확인
                    val isFollowed = followingsList.any { it.memberId == targetMemberId }

                    Log.d("ProfileViewModel", "팔로우 상태 확인 결과: isFollowed=$isFollowed")

                    // 상태 업데이트
                    _isFollowing.value = isFollowed
                } else {
                    Log.e("ProfileViewModel", "팔로우 상태 확인 실패: ${followingsResponse.message}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "팔로우 상태 확인 중 예외 발생", e)
            }
        }
    }

    // 검색 프로필 목록을 불러오는 함수
    fun searchProfiles(query: String, page: Int = 0, size: Int = 10) {
        // 기존 검색 작업 취소
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            try {
                delay(300) // 디바운스

                val searchResponse = profileService.searchProfile(query, page, size)
                if (searchResponse.code == "SU") {
                    _searchResults.value = searchResponse.payload ?: emptyList()
                } else {
                    // 에러가 발생해도 결과만 비움 (에러 메시지 설정 없음)
                    _searchResults.value = emptyList()
                }
            } catch (e: Exception) {
                // 예외 발생 시에도 결과만 비움 (에러 메시지 설정 없음)
                _searchResults.value = emptyList()
            }
        }
    }

    // 명시적으로 검색 결과와 에러 메시지 초기화
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // 검색 결과에서 유저가 팔로우 중인지 확인
    fun isUserFollowed(userId: Int): Boolean {
        return myFollowings.value.any { it.memberId == userId }
    }

    fun toggleFollow(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                if (!ensureValidUserId()) return@launch

                // 변경하기 전 현재 상태 저장
                val currentFollowState = _isFollowing.value

                // UI를 즉시 업데이트하여 반응성 높이기
                _isFollowing.value = !currentFollowState

                // 팔로워 카운트도 선제적으로 업데이트
                if (!currentFollowState) { // 팔로우 -> 팔로워 수 증가
                    _followerCount.value = _followerCount.value + 1
                } else { // 언팔로우 -> 팔로워 수 감소
                    _followerCount.value = Math.max(0, _followerCount.value - 1)
                }

                // 현재 상태의 반대로 팔로우 요청 생성
                val followRequest = ProfileRequest.FollowRequest(
                    memberId = targetMemberId,
                    follow = !currentFollowState
                )

                Log.d("ProfileViewModel", "팔로우 상태 변경 중: targetId=$targetMemberId, 현재상태=$currentFollowState, 요청=${!currentFollowState}")

                // API 호출
                val response = profileService.follow(followRequest)

                if (response.code == "SU") {
                    Log.d("ProfileViewModel", "팔로우 상태 변경 성공: ${!currentFollowState}")

                    // 서버와 동기화하기 위해 팔로우 리스트 새로고침
                    loadMyFollowings()
                } else {
                    // API 호출이 실패하면 UI 변경 되돌리기
                    _isFollowing.value = currentFollowState

                    // 팔로워 카운트도 되돌리기
                    if (!currentFollowState) { // 팔로우 시도 실패
                        _followerCount.value = _followerCount.value - 1
                    } else { // 언팔로우 시도 실패
                        _followerCount.value = _followerCount.value + 1
                    }

                    _errorMessage.value = "팔로우 변경 실패: ${response.message}"
                    Log.e("ProfileViewModel", "팔로우 상태 변경 실패: ${response.message}")
                }
            } catch (e: Exception) {
                // 예외 처리 및 UI 상태 되돌리기
                _isFollowing.value = !_isFollowing.value
                _errorMessage.value = "네트워크 오류: ${e.message}"
                Log.e("ProfileViewModel", "toggleFollow에서 예외 발생", e)
            }
        }
    }
}