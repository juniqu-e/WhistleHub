package com.whistlehub.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.request.ProfileRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.repository.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ProfileChangeViewModel @Inject constructor(
    private val profileService: ProfileService,
    val userRepository: UserRepository
) : ViewModel() {
    // 상태 관리 변수들
    val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    val _profileText = MutableStateFlow("")
    val profileText: StateFlow<String> = _profileText.asStateFlow()

    val _profileImageUrl = MutableStateFlow("")
    val profileImageUrl: StateFlow<String> = _profileImageUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 닉네임 및 자기소개 업데이트 함수
    fun updateProfile(nickname: String, profileText: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = ProfileRequest.UpdateProfileRequest(nickname, profileText)
                val response = profileService.updateProfile(request)
                if (response.code == "SU") {
                    // 성공적으로 업데이트된 경우
                    _nickname.value = nickname
                    _profileText.value = profileText
                    _errorMessage.value = ""
                } else {
                    // 실패 시 에러 메시지 처리
                    _errorMessage.value = "프로필 업데이트 실패"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 프로필 이미지 업데이트 함수
    fun updateProfileImage(image: MultipartBody.Part) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = profileService.uploadProfileImage(
                    memberId = userRepository.getUser()?.memberId ?: return@launch,
                    image = image
                )

                if (response.code == "SU") {
                    // 성공적으로 이미지 업로드된 경우 URL 업데이트
                    val imageUrl = response.payload ?: ""
                    _profileImageUrl.value = imageUrl
                    _errorMessage.value = ""
                } else {
                    // 실패 시 에러 메시지 처리
                    _errorMessage.value = "이미지 업로드 실패"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 프로필 이미지 삭제 요청
    fun deleteProfileImage() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = profileService.deleteProfileImage(
                    memberId = userRepository.getUser()?.memberId ?: return@launch
                )

                if (response.code == "SU") {
                    // 성공적으로 이미지 삭제된 경우 URL 초기화
                    _profileImageUrl.value = ""
                    _errorMessage.value = ""
                } else {
                    // 실패 시 에러 메시지 처리
                    _errorMessage.value = "이미지 삭제 실패"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 초기 데이터 로드 함수 (예: 기존 프로필 정보 가져오기)
    fun loadProfile(memberId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = profileService.getProfile(memberId)
                if (response.code == "SU") {
                    val profileData = response.payload ?: return@launch
                    _nickname.value = profileData.nickname ?: ""
                    _profileText.value = profileData.profileText ?: ""
                    _profileImageUrl.value = profileData.profileImage ?: ""
                } else {
                    _errorMessage.value = "프로필 정보를 불러오지 못했습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "알 수 없는 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
