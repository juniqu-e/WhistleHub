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
import okhttp3.MultipartBody
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

    // Track update related states
    private val _isUpdateLoading = MutableStateFlow(false)
    val isUpdateLoading: StateFlow<Boolean> = _isUpdateLoading

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    // Track deletion related states
    private val _isDeleteLoading = MutableStateFlow(false)
    val isDeleteLoading: StateFlow<Boolean> = _isDeleteLoading

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    // Track image upload related states
    private val _isImageUploadLoading = MutableStateFlow(false)
    val isImageUploadLoading: StateFlow<Boolean> = _isImageUploadLoading

    private val _imageUploadSuccess = MutableStateFlow(false)
    val imageUploadSuccess: StateFlow<Boolean> = _imageUploadSuccess

    private val _isImageUploading = MutableStateFlow(false)
    val isImageUploading: StateFlow<Boolean> = _isImageUploading

    fun initTrackDetails(isLiked: Boolean, likeCount: Int) {
        _isLiked.value = isLiked
        _likeCount.value = likeCount
    }

    suspend fun loadTrackDetails(trackId: Int) {
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

    // Track Information Update Function
    fun updateTrackInfo(trackId: Int, title: String, description: String, visibility: Boolean) {
        viewModelScope.launch {
            try {
                _isUpdateLoading.value = true
                _updateSuccess.value = false
                _errorMessage.value = null

                val request = TrackRequest.UpdateTrackRequest(
                    trackId = trackId.toString(),
                    title = title,
                    description = description,
                    visibility = visibility
                )

                val response = trackService.updateTrack(request)

                if (response.code == "SU") {
                    // Successfully updated track information
                    _updateSuccess.value = true

                    // Reload track details to get updated information
                    loadTrackDetails(trackId)
                } else {
                    _errorMessage.value = response.message ?: "Failed to update track information"
                    Log.e("ProfileTrackDetailViewModel", "Error updating track: ${response.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred while updating track"
                Log.e("ProfileTrackDetailViewModel", "Exception in updateTrackInfo", e)
            } finally {
                _isUpdateLoading.value = false
            }
        }
    }

    // Track Deletion Function
    fun deleteTrack(trackId: Int) {
        viewModelScope.launch {
            try {
                _isDeleteLoading.value = true
                _deleteSuccess.value = false
                _errorMessage.value = null

                val response = trackService.deleteTrack(trackId.toString())

                if (response.code == "SU") {
                    // Successfully deleted track
                    _deleteSuccess.value = true
                    Log.d("ProfileTrackDetailViewModel", "Track deleted successfully: $trackId")
                } else {
                    _errorMessage.value = response.message ?: "Failed to delete track"
                    Log.e("ProfileTrackDetailViewModel", "Error deleting track: ${response.message}")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An error occurred while deleting track"
                Log.e("ProfileTrackDetailViewModel", "Exception in deleteTrack", e)
            } finally {
                _isDeleteLoading.value = false
            }
        }
    }

    // Update the updateTrackInfo method to include the image parameter
    fun updateTrackInfo(
        trackId: Int,
        title: String,
        description: String,
        visibility: Boolean,
        image: MultipartBody.Part? = null
    ) {
        viewModelScope.launch {
            try {
                _isUpdateLoading.value = true

                // If there's an image to upload, do that first
                if (image != null) {
                    _isImageUploading.value = true
                    val imageResponse = trackService.uploadTrackImage(trackId, image)
                    _isImageUploading.value = false

                    if (imageResponse.code != "SU") {
                        _errorMessage.value = "Failed to upload image: ${imageResponse.message}"
                        _isUpdateLoading.value = false
                        return@launch
                    }
                }

                // Now update the track info
                val request = TrackRequest.UpdateTrackRequest(
                    trackId = trackId.toString(),
                    title = title,
                    description = description,
                    visibility = visibility
                )

                val response = trackService.updateTrack(request)

                if (response.code == "SU") {
                    // Reload track details to get the updated info
                    loadTrackDetails(trackId)
                    _updateSuccess.value = true
                } else {
                    _errorMessage.value = response.message
                }

                _isUpdateLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error updating track: ${e.message}"
                _isUpdateLoading.value = false
            }
        }
    }

    // Add a helper method just for uploading the image if needed separately
    fun uploadTrackImage(trackId: Int, image: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                _isImageUploading.value = true
                val response = trackService.uploadTrackImage(trackId, image)
                _isImageUploading.value = false

                if (response.code == "SU") {
                    // Reload track details to get the updated image URL
                    loadTrackDetails(trackId)
                } else {
                    _errorMessage.value = "Failed to upload image: ${response.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error uploading image: ${e.message}"
                _isImageUploading.value = false
            }
        }
    }

    // Reset update/delete/upload status (call after handling UI responses)
    fun resetUpdateStatus() {
        _updateSuccess.value = false
    }

    fun resetDeleteStatus() {
        _deleteSuccess.value = false
    }

    fun resetImageUploadStatus() {
        _imageUploadSuccess.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}