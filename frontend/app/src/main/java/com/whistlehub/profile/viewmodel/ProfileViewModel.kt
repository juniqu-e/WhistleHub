package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.util.SntpClient.isInitialized
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

    private val _followerCount = MutableStateFlow(0)
    val followerCount: StateFlow<Int> get() = _followerCount

    private val _followingCount = MutableStateFlow(0)
    val followingCount: StateFlow<Int> get() = _followingCount

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        // 초기화 작업 한 번만 실행
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user != null) {
                _memberId.value = user.memberId
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
                        currentTracks.addAll(tracksResponse.payload ?: emptyList())
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
                    // 페이지가 0이면 초기화, 아니면 기존 목록에 추가
                    if (page == 0) {
                        _followers.emit(followersResponse.payload ?: emptyList())
                    } else {
                        val currentFollowers = _followers.value.toMutableList()
                        currentFollowers.addAll(followersResponse.payload ?: emptyList())
                        _followers.emit(currentFollowers)
                    }
                } else {
                    _errorMessage.value = followersResponse.message
                    Log.e(
                        "ProfileViewModel",
                        "Failed to load followers: ${followersResponse.message}"
                    )
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
                    // 페이지가 0이면 초기화, 아니면 기존 목록에 추가
                    if (page == 0) {
                        _followings.emit(followingsResponse.payload ?: emptyList())
                    } else {
                        val currentFollowings = _followings.value.toMutableList()
                        currentFollowings.addAll(followingsResponse.payload ?: emptyList())
                        _followings.emit(currentFollowings)
                    }
                } else {
                    _errorMessage.value = followingsResponse.message
                    Log.e(
                        "ProfileViewModel",
                        "Failed to load followings: ${followingsResponse.message}"
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
                Log.e("ProfileViewModel", "Exception while loading followings", e)
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

    // ProfileViewModel.kt 내의 toggleFollow 메서드 수정
    fun toggleFollow(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                if (!ensureValidUserId()) return@launch

                val currentlyFollowing = _isFollowing.value
                val followRequest = ProfileRequest.FollowRequest(
                    memberId = targetMemberId,
                    follow = !currentlyFollowing
                )

                Log.d("ProfileService", "팔로우 API 호출: targetId=${targetMemberId}, action=${!currentlyFollowing}")
                val response = profileService.follow(followRequest)

                // 에러 처리 개선
                if (response.code == "SU") {
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