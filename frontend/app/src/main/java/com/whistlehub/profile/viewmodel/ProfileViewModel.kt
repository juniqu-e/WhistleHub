package com.whistlehub.profile.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.data.repository.ProfileService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileService: ProfileService
) : ViewModel() {
    var profileState: ProfileResponse.GetProfileResponse by mutableStateOf(
        ProfileResponse.GetProfileResponse(
            nickname = "",
            profileImage = "",
            profileText = ""
        )
    )
        private set

    var trackListState by mutableStateOf<List<ProfileResponse.GetMemberTracksResponse>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadProfile(memberId: Int? = null) {
        viewModelScope.launch {
            loading = true
            val profileResponse = profileService.getProfile(memberId)
            val tracksResponse = profileService.getMemberTracks(memberId, page = 1, orderby = "latest")

            if (profileResponse.payload != null && tracksResponse.payload != null) {
                profileState = profileResponse.payload
                trackListState = tracksResponse.payload
                errorMessage = null
            } else {
                errorMessage = profileResponse.message ?: "프로필 데이터를 불러오는데 실패했습니다."
            }

            loading = false
        }
    }
}
