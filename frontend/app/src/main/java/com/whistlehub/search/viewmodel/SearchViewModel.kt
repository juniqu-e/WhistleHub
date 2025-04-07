package com.whistlehub.search.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.data.repository.TrackService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trackService: TrackService
) : ViewModel() {
    private val _searchResult = MutableStateFlow<List<TrackResponse.SearchTrack>>(emptyList())
    val searchResult: StateFlow<List<TrackResponse.SearchTrack>> get() = _searchResult
    private val _tagList = MutableStateFlow<List<AuthResponse.TagResponse>>(emptyList())
    val tagList: StateFlow<List<AuthResponse.TagResponse>> get() = _tagList
    private val _tagRanking =
        MutableStateFlow<List<TrackResponse.SearchTrack>>(emptyList())
    val tagRanking: StateFlow<List<TrackResponse.SearchTrack>> get() = _tagRanking
    private val _tagRecommendTrack =
        MutableStateFlow<List<TrackResponse.SearchTrack>>(emptyList())
    val tagRecommendTrack: StateFlow<List<TrackResponse.SearchTrack>> get() = _tagRecommendTrack
    private val _tagRecentTrack =
        MutableStateFlow<List<TrackResponse.SearchTrack>>(emptyList())
    val tagRecentTrack: StateFlow<List<TrackResponse.SearchTrack>> get() = _tagRecentTrack
    private val _trackDetail = MutableStateFlow<TrackResponse.GetTrackDetailResponse?>(null)
    val trackDetail: StateFlow<TrackResponse.GetTrackDetailResponse?> get() = _trackDetail

    // 트랙 검색
    suspend fun searchTracks(keyword: String) {
        try {
            val response = trackService.searchTracks(
                TrackRequest.SearchTrackRequest(
                    keyword = keyword,
                    page = 0,
                    size = 50,
                    orderBy = "DESC"
                )
            )
            if (response.code == "SU") {
                _searchResult.value = response.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to search tracks: ${response.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to search tracks: ${e.message}")
        }
    }

    fun getTrackDetails(trackId: Int) {
        Log.d("detail", trackId.toString())
        Log.d("detail", "Scope active? ${viewModelScope.coroutineContext[Job]?.isActive}")
        viewModelScope.launch {
            try {
                val response = trackService.getTrackDetail(trackId.toString())
                Log.d("detail", response.payload.toString())
                if (response.code == "SU" && response.payload != null) {
                    _trackDetail.value = response.payload
                } else {
                    return@launch
                }
            } catch (e: Exception) {
                return@launch
            }
        }
    }

    fun clearTrackDetail() {
        _trackDetail.value = null
    }

    // 태그 추천 받기
    suspend fun recommendTag() {
        try {
            val response = trackService.getTagRecommendation()
            if (response.code == "SU") {
                // 태그 추천 성공
                _tagList.value = response.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to recommend tags: ${response.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to recommend tags: ${e.message}")
        }
    }

    // 태그별 랭킹 받기
    suspend fun getRankingByTag(tagId: Int, period: String = "WEEK") {
        try {
            val response = trackService.getTagRanking(
                tagId = tagId,
                period = period,
                page = 0,
                size = 50
            )
            if (response.code == "SU") {
                // 태그 랭킹 성공
                _tagRanking.value = response.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to get tag ranking: ${response.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get tag ranking: ${e.message}")
        }
    }

    // 태그별 추천 트랙 받기
    suspend fun getTagRecommendTrack(tagId: Int) {
        try {
            val response = trackService.getTagRecommendTrack(
                tagId = tagId,
                size = 3
            )
            if (response.code == "SU") {
                // 태그 추천 트랙 성공
                _tagRecommendTrack.value = response.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to get tag recommend track: ${response.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get tag recommend track: ${e.message}")
        }
    }

    // 태그별 최신 트랙 받기
    suspend fun getTagRecentTrack(tagId: Int) {
        try {
            val response = trackService.getTagRecentTracks(
                tagId = tagId,
                size = 10
            )
            if (response.code == "SU") {
                // 태그 추천 트랙 성공
                _tagRecentTrack.value = response.payload ?: emptyList()
            } else {
                Log.d("error", "Failed to get tag recommend track: ${response.message}")
            }
        } catch (e: Exception) {
            Log.d("error", "Failed to get tag recommend track: ${e.message}")
        }
    }
}