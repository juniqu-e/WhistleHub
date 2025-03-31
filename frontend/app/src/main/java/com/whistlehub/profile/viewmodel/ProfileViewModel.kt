package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.remote.dto.response.ProfileResponse
import com.whistlehub.common.data.repository.ProfileService
import com.whistlehub.common.viewmodel.LoginViewModel
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

    private val _tracks = MutableStateFlow<List<ProfileResponse.GetMemberTracksResponse>>(emptyList())
    val tracks: StateFlow<List<ProfileResponse.GetMemberTracksResponse>> get() = _tracks

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    fun loadProfile(memberId: Int) {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user == null) {
                Log.d("warning", "User not found")
            }
            val profileResponse = profileService.getProfile(user?.memberId ?: 0)
            withContext(Dispatchers.Main) {
                if (profileResponse.code == "SU") {
                    _profile.emit(profileResponse.payload)
                } else {
                    _errorMessage.value = profileResponse.message
                }
            }
        }
    }

    fun loadTracks(memberId: Int, page: Int = 0, size: Int = 9) {
        viewModelScope.launch {
            val user = userRepository.getUser()
            if (user == null) {
                Log.d("warning", "User not found")
            }
            val tracksResponse = profileService.getMemberTracks(user?.memberId ?:0, page, size)
            if (tracksResponse.code == "SU") {
                _tracks.emit(tracksResponse.payload ?: emptyList())
            } else {
                _errorMessage.value = tracksResponse.message
            }
        }
    }
}
