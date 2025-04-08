package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.repository.ProfileService
import com.whistlehub.common.util.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileMenuViewModel @Inject constructor(
    private val profileService: ProfileService,
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    /**
     * 회원 탈퇴 API를 호출하고 결과를 반환하는 함수
     *
     * @return DeleteAccountResult 객체 (성공/실패 여부와 오류 메시지 포함)
     */
    suspend fun deleteAccount(): DeleteAccountResult {
        return try {
            val response = profileService.deleteProfile()

            if (response.code == "SU") {
                // 성공 시 토큰과 유저 데이터 삭제
                // 참고: LogoutManager.emitLogout()은 이 ViewModel을 호출하는 측에서 처리
                Log.d("ProfileMenuViewModel", "회원 탈퇴 성공")
                DeleteAccountResult(true)
            } else {
                // API 호출은 성공했지만 서버 측 오류 발생
                Log.e("ProfileMenuViewModel", "회원 탈퇴 실패: ${response.message}")
                DeleteAccountResult(false, response.message)
            }
        } catch (e: Exception) {
            // 네트워크 오류 등 예외 발생
            Log.e("ProfileMenuViewModel", "회원 탈퇴 중 예외 발생", e)
            DeleteAccountResult(false, e.message ?: "네트워크 오류가 발생했습니다.")
        }
    }
}

/**
 * 회원 탈퇴 결과를 나타내는 데이터 클래스
 */
data class DeleteAccountResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)