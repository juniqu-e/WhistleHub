package com.whistlehub.search.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trackService: TrackService
) : ViewModel() {
    private val _searchResult = MutableStateFlow<List<TrackResponse.SearchTrack>>(emptyList())
    val searchResult: StateFlow<List<TrackResponse.SearchTrack>> get() = _searchResult

    // 트랙 검색
    suspend fun searchTracks(keyword: String) {
        try {
            val response = trackService.searchTracks(keyword)
            if (response.code == "SU") {
                _searchResult.value = response.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to search tracks: ${response.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to search tracks: ${e.message}")
        }
    }
}