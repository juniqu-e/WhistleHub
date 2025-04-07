package com.whistlehub.common.data.repository

import com.whistlehub.common.data.remote.api.TrackApi
import com.whistlehub.common.data.remote.dto.request.TrackRequest
import com.whistlehub.common.data.remote.dto.response.ApiResponse
import com.whistlehub.common.data.remote.dto.response.AuthResponse
import com.whistlehub.common.data.remote.dto.response.TrackResponse
import com.whistlehub.common.util.TokenRefresh
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
---------------------------------------
트랙 관련 API 호출을 담당하는 서비스 클래스
---------------------------------------
 **/
@Singleton
class TrackService @Inject constructor(
    private val trackApi: TrackApi,
    private val tokenRefresh: TokenRefresh
) : ApiRepository() {
    // 트랙 상세 조회
    suspend fun getTrackDetail(
        trackId: String
    ): ApiResponse<TrackResponse.GetTrackDetailResponse> {
        return tokenRefresh.execute { trackApi.getTrackDetail(trackId) }
    }

    // 트랙 정보 수정
    suspend fun updateTrack(
        request: TrackRequest.UpdateTrackRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.updateTrack(request) }
    }

    // 트랙 삭제
    suspend fun deleteTrack(
        trackId: String
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.deleteTrack(trackId) }
    }

    // 트랙 재생 요청
    suspend fun playTrack(
        trackId: String,
    ): ByteArray? {
        val response = trackApi.playTrack(trackId) // API 호출
        return if (response.isSuccessful) {
            response.body()?.bytes() // 성공하면 ByteArray 반환
        } else {
            null // 실패 시 null 반환
        }
    }

    // 트랙 재생 카운트 증가
    suspend fun increasePlayCount(
        request: TrackRequest.TrackPlayCountRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.increasePlayCount(request) }
    }

    // 플레이리스트에 트랙 추가
    suspend fun addTrackToPlaylist(
        request: TrackRequest.AddTrackToPlaylistRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.addTrackToPlaylist(request) }
    }

    // 트랙 레이어 조회
    suspend fun getTrackLayers(
        trackId: String
    ): ApiResponse<List<TrackResponse.GetTrackLayer>> {
        return tokenRefresh.execute { trackApi.getTrackLayers(trackId) }
    }

    // 트랙 레이어 재생
    suspend fun playLayer(
        layerId: String
    ): ApiResponse<TrackResponse.TrackLayerPlay> {
        return tokenRefresh.execute { trackApi.playLayer(layerId) }
    }

    // 트랙 좋아요
    suspend fun likeTrack(
        request: TrackRequest.LikeTrackRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.likeTrack(request) }
    }

    // 트랙 좋아요 취소
    suspend fun unlikeTrack(
        trackId: String
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.unlikeTrack(trackId) }
    }

    // 트랙 댓글 조회
    suspend fun getTrackComments(
        trackId: String
    ): ApiResponse<List<TrackResponse.GetTrackComment>> {
        return tokenRefresh.execute { trackApi.getTrackComments(trackId) }
    }

    // 트랙 댓글 작성
    suspend fun createTrackComment(
        request: TrackRequest.CreateCommentRequest
    ): ApiResponse<Int> {
        return tokenRefresh.execute { trackApi.createTrackComment(request) }
    }

    // 트랙 댓글 수정
    suspend fun updateTrackComment(
        request: TrackRequest.UpdateCommentRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.updateTrackComment(request) }
    }

    // 트랙 댓글 삭제
    suspend fun deleteTrackComment(
        commentId: String
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.deleteTrackComment(commentId) }
    }

    // 트랙 검색
//    suspend fun searchTracks(
//        request: TrackRequest.SearchTrackRequest
//    ): ApiResponse<List<TrackResponse.SearchTrack>> {
//        return tokenRefresh.execute { trackApi.searchTracks(request) }
//    }
    suspend fun searchTracks(
        request: TrackRequest.SearchTrackRequest
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute {
            trackApi.searchTracks(
                keyword = request.keyword,
                page = request.page,
                size = request.size,
                orderBy = request.orderBy,
            )
        }
    }

    // 트랙 신고
    suspend fun reportTrack(
        request: TrackRequest.ReportTrackRequest
    ): ApiResponse<Unit> {
        return tokenRefresh.execute { trackApi.reportTrack(request) }
    }

    // 트랙 이미지 업로드
    suspend fun uploadTrackImage(
        trackId: Int,
        image: MultipartBody.Part
    ): ApiResponse<String> {
        val trackIdBody: RequestBody =
            trackId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        return tokenRefresh.execute { trackApi.uploadTrackImage(trackIdBody, image) }
    }

    // 태그 추천 조회
    suspend fun getTagRecommendation(): ApiResponse<List<AuthResponse.TagResponse>> {
        return tokenRefresh.execute { trackApi.getTagRecommendation() }
    }

    // 태그별 랭킹 조회
    suspend fun getTagRanking(
        tagId: Int,
        period: String,
        page: Int,
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getTagRanking(tagId, period, page, size) }
    }

    // 태그별 추천 트랙 조회
    suspend fun getTagRecommendTrack(
        tagId: Int,
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getTagRecommendTrack(tagId, size) }
    }

    // 최근 들은 트랙 조회
    suspend fun getRecentTracks(
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getRecentTracks(size) }
    }

    // 특정 트랙과 비슷한 트랙 리스트 조회
    suspend fun getSimilarTracks(
        trackId: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getSimilarTracks(trackId) }
    }

    // 한 번도 듣지 않은 트랙 조회
    suspend fun getNeverTracks(
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getNeverTracks(size) }
    }

    // 팔로우한 사람 중 한 명 조회
    suspend fun getFollowingMember(
    ): ApiResponse<TrackResponse.MemberInfo> {
        return tokenRefresh.execute { trackApi.getFollowingMember() }
    }

    // 특정 회원을 팔로우한 사람들이 좋아하는 트랙 리스트 조회
    suspend fun getFanMixTracks(
        memberId: Int,
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getFanMixTracks(memberId, size) }
    }

    // 팔로우한 사람의 최신 트랙 조회
    suspend fun getFollowingRecentTracks(
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getFollowingRecentTracks(size) }
    }

    // 태그별 최신 트랙 조회
    suspend fun getTagRecentTracks(
        tagId: Int,
        size: Int
    ): ApiResponse<List<TrackResponse.SearchTrack>> {
        return tokenRefresh.execute { trackApi.getTagRecentTracks(tagId, size) }
    }
}
