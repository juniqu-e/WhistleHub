package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.data.repository.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // 검색 결과 상태 추가
    private val _searchResults = MutableStateFlow<List<ProfileResponse.SearchProfileResponse>>(emptyList())
    val searchResults: StateFlow<List<ProfileResponse.SearchProfileResponse>> get() = _searchResults

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
                    _profile.emit(profileResponse.payload)

                    // 기본 정보 로드 후 뒤이어 팔로워/팔로잉 카운트 로드
                    loadFollowerCount(targetMemberId)
                    loadFollowingCount(targetMemberId)

                    // 프로필 로드 완료 후에만 팔로우 상태 체크
                    if (targetMemberId != _memberId.value) {
                        checkFollowStatus(targetMemberId)
                    }
                }
            } catch (
                e: Exception
            ) {
                Log.e("ProfileViewModel", "Exception while loading profile", e)
            }
        }
    }

    private fun loadFollowerCount(memberId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading follower count for memberId: $memberId")
                val followersResponse = profileService.getFollowers(memberId, 0, 999)
                if (followersResponse.code == "SU") {
                    val count = followersResponse.payload?.size ?: 0
                    _followerCount.emit(count)
                    _followers.emit(followersResponse.payload ?: emptyList())
                    Log.d("ProfileViewModel", "Follower count loaded: $count")
                } else {
                    Log.e(
                        "ProfileViewModel",
                        "Failed to load follower count: ${followersResponse.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception while loading follower count", e)
            }
        }
    }

    private fun loadFollowingCount(memberId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Loading following count for memberId: $memberId")
                val followingsResponse = profileService.getFollowings(memberId, 0, 999)
                if (followingsResponse.code == "SU") {
                    val count = followingsResponse.payload?.size ?: 0
                    _followingCount.emit(count)
                    _followings.emit(followingsResponse.payload ?: emptyList())
                    Log.d("ProfileViewModel", "Following count loaded: $count")
                } else {
                    Log.e(
                        "ProfileViewModel",
                        "Failed to load following count: ${followingsResponse.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception while loading following count", e)
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
                if (!ensureValidUserId()) return@launch
                Log.d(
                    "ProfileViewModel",
                    "Checking follow status: current user=${_memberId.value}, target user=$targetMemberId"
                )

                // Early return if we're trying to check our own profile
                if (targetMemberId == _memberId.value) {
                    Log.d("ProfileViewModel", "Cannot follow your own profile")
                    _isFollowing.emit(false)
                    return@launch
                }

                // Get current user's following list to check if target user is in it
                val followingsResponse = profileService.getFollowings(_memberId.value, 0, 999)
                if (followingsResponse.code == "SU") {
                    val isFollowed =
                        followingsResponse.payload?.any { it.memberId == targetMemberId } ?: false
                    _isFollowing.emit(isFollowed)
                    Log.d("ProfileViewModel", "Follow status check - isFollowing: $isFollowed")
                } else {
                    Log.e(
                        "ProfileViewModel",
                        "Failed to check follow status: ${followingsResponse.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception while checking follow status", e)
            }
        }
    }

    // 검색 프로필 목록을 불러오는 함수
    // ProfileViewModel.kt의 searchProfiles 함수 수정
    fun searchProfiles(query: String, page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            try {
                Log.d("ProfileViewModel", "Searching profiles with query: $query")

                // 중요: 검색 시작 시 로딩 상태 명시적으로 설정
                // 상태를 내보내는 함수가 있다면 사용

                val searchResponse = profileService.searchProfile(query, page, size)

                if (searchResponse.code == "SU") {
                    // 중요: 상태 값을 즉시 업데이트하고 UI에 반영되도록 함
                    // value를 직접 수정하는 것이 emit보다 즉각적일 수 있음
                    _searchResults.value = searchResponse.payload ?: emptyList()
                    Log.d("ProfileViewModel", "Search returned ${searchResponse.payload?.size ?: 0} results")
                } else {
                    _errorMessage.value = searchResponse.message
                    Log.e("ProfileViewModel", "Failed to search profiles: ${searchResponse.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "검색 중 오류가 발생했습니다."
                Log.e("ProfileViewModel", "Exception while searching profiles", e)
            }
        }
    }

    // 검색 결과 초기화
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    // 검색 결과에서 유저가 팔로우 중인지 확인
    fun isUserFollowed(userId: Int): Boolean {
        return myFollowings.value.any { it.memberId == userId }
    }

    // ProfileViewModel.kt 내의 toggleFollow 메서드 수정
    fun toggleFollow(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                if (!ensureValidUserId()) return@launch

                val currentlyFollowing = isUserFollowed(targetMemberId)
                val followRequest = ProfileRequest.FollowRequest(
                    memberId = targetMemberId,
                    follow = !currentlyFollowing
                )

                Log.d("ProfileService", "팔로우 API 호출: targetId=${targetMemberId}, action=${!currentlyFollowing}")
                val response = profileService.follow(followRequest)

                // 에러 처리 개선
                if (response.code == "SU") {
                    loadMyFollowings()
                    _isFollowing.emit(!currentlyFollowing)

                    // 팔로워 카운트 업데이트
                    if (!currentlyFollowing) { // 팔로우 -> 팔로워 수 증가
                        _followerCount.emit(_followerCount.value + 1)
                    } else { // 언팔로우 -> 팔로워 수 감소
                        _followerCount.emit(Math.max(0, _followerCount.value - 1))
                    }
                } else {
                    _errorMessage.value = "팔로우 변경 실패: ${response.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류: ${e.message}"
            }
        }
    }
}