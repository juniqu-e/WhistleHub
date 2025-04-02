package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.data.repository.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileService: ProfileService,
    val userRepository: UserRepository
) : ViewModel() {

    private val _memberId = MutableStateFlow(0)
    val memberId: StateFlow<Int> get() = _memberId

    private val _isLogin = MutableStateFlow(true)
    val isLogin: StateFlow<Boolean> get() = _isLogin

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

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> get() = _isFollowing

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    init {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user != null) {
                _memberId.value = user.memberId
            } else {
                Log.d("ProfileViewModel", "User not found in local storage")
            }
        }
    }

    fun loadProfile(memberId: Int) {
        viewModelScope.launch {
            try {
                val profileResponse = profileService.getProfile(memberId)
                if (profileResponse.code == "SU") {
                    _profile.emit(profileResponse.payload)
                } else {
                    _errorMessage.value = profileResponse.message
                    Log.e("ProfileViewModel", "Failed to load profile: ${profileResponse.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
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
                // 팔로잉 목록을 가져와서 현재 사용자가 타겟 사용자를 팔로우하고 있는지 확인
                val followingsResponse = profileService.getFollowings(_memberId.value, 0, 999999)
                if (followingsResponse.code == "SU") {
                    val isFollowing =
                        followingsResponse.payload?.any { it.memberId == targetMemberId } ?: false
                    _isFollowing.emit(isFollowing)
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

    fun toggleFollow(targetMemberId: Int) {
        viewModelScope.launch {
            try {
                val currentlyFollowing = _isFollowing.value
                val followRequest = ProfileRequest.FollowRequest(
                    memberId = targetMemberId,
                    follow = !currentlyFollowing
                )

                val response = profileService.follow(followRequest)
                if (response.code == "SU") {
                    // 팔로우 상태 업데이트
                    _isFollowing.emit(!currentlyFollowing)

                    // 팔로워/팔로잉 목록 업데이트
                    if (currentlyFollowing) {
                        // 언팔로우한 경우, 상대방의 팔로워 목록에서 나를 제거
                        val updatedFollowers = _followers.value.toMutableList()
                        updatedFollowers.removeAll { it.memberId == _memberId.value }
                        _followers.emit(updatedFollowers)
                    } else {
                        // 팔로우한 경우, 상대방의 팔로워 목록에 나를 추가
                        // 서버에서 최신 상태를 받아오는 방식으로 구현
                        loadFollowers(targetMemberId, 0)
                    }
                } else {
                    _errorMessage.value = response.message
                    Log.e("ProfileViewModel", "Failed to toggle follow: ${response.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
                Log.e("ProfileViewModel", "Exception while toggling follow", e)
            }
        }
    }
}