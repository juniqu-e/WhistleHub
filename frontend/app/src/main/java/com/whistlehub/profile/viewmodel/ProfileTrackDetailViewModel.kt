package com.whistlehub.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileTrackDetailViewModel @Inject constructor(
    private val trackService: TrackService
) : ViewModel() {

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked

    private val _likeCount = MutableStateFlow(0)
    val likeCount: StateFlow<Int> = _likeCount

    private val _trackDetail = MutableStateFlow<TrackResponse.GetTrackDetailResponse?>(null)
    val trackDetail: StateFlow<TrackResponse.GetTrackDetailResponse?> = _trackDetail

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun initTrackDetails(isLiked: Boolean, likeCount: Int) {
        _isLiked.value = isLiked
        _likeCount.value = likeCount
    }

    fun loadTrackDetails(trackId: Int) {
        viewModelScope.launch {
            try {
                val response = trackService.getTrackDetail(trackId.toString())
                if (response.code == "SU" && response.payload != null) {
                    _trackDetail.value = response.payload
                    _isLiked.value = response.payload.isLiked
                    _likeCount.value = response.payload.likeCount
                } else {
                    _errorMessage.value = response.message ?: "Failed to load track details"
                    Log.e("ProfileTrackDetailViewModel", "Error loading track details: ${response.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred"
                Log.e("ProfileTrackDetailViewModel", "Exception in loadTrackDetails", e)
            }
        }
    }

    fun toggleLike(trackId: Int) {
        viewModelScope.launch {
            try {
                if (_isLiked.value) {
                    // Unlike track
                    val response = trackService.unlikeTrack(trackId.toString())
                    if (response.code == "SU") {
                        _isLiked.value = false
                        _likeCount.value = _likeCount.value - 1
                    } else {
                        _errorMessage.value = response.message
                        Log.e("ProfileTrackDetailViewModel", "Error unliking track: ${response.message}")
                    }
                } else {
                    // Like track
                    val request = TrackRequest.LikeTrackRequest(trackId)
                    val response = trackService.likeTrack(request)
                    if (response.code == "SU") {
                        _isLiked.value = true
                        _likeCount.value = _likeCount.value + 1
                    } else {
                        _errorMessage.value = response.message
                        Log.e("ProfileTrackDetailViewModel", "Error liking track: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                Log.e("ProfileTrackDetailViewModel", "Exception in toggleLike", e)
            }
        }
    }
}