package com.whistlehub.common.data.remote.api

import retrofit2.Response
import retrofit2.http.*

/**
-----------------------
트랙 관련 API 인터페이스
-----------------------
 **/

interface TrackApi {

    @GET("/track")
    suspend fun getTrackDetail(@Query("trackId") trackId: String): Response<Any>

    @PUT("/track")
    suspend fun updateTrack(@Body requestBody: Any): Response<Any>

    @DELETE("/track")
    suspend fun deleteTrack(@Query("trackId") trackId: String): Response<Any>

    @POST("/track/search")
    suspend fun searchTracks(@Body requestBody: Any): Response<Any>

    @POST("/track/like")
    suspend fun likeTrack(@Body requestBody: Any): Response<Any>

    @POST("/track/report")
    suspend fun reportTrack(@Body requestBody: Any): Response<Any>

    @POST("/track/playlist")
    suspend fun addTrackToPlaylist(@Body requestBody: Any): Response<Any>

    @GET("/track/comment")
    suspend fun getTrackComments(@Query("trackId") trackId: String): Response<Any>

    @POST("/track/comment")
    suspend fun createTrackComment(@Body requestBody: Any): Response<Any>

    @PUT("/track/comment")
    suspend fun updateTrackComment(@Body requestBody: Any): Response<Any>

    @DELETE("/track/comment")
    suspend fun deleteTrackComment(@Query("commentId") commentId: String): Response<Any>

    @GET("/track/play")
    suspend fun playTrack(@Query("trackId") trackId: String): Response<Any>

    @POST("/track/play")
    suspend fun increasePlayCount(@Body requestBody: Any): Response<Any>

    @GET("/track/like")
    suspend fun getTrackLikeStatus(@Query("trackId") trackId: String): Response<Any>

    @GET("/track/layer")
    suspend fun getTrackLayers(@Query("trackId") trackId: String): Response<Any>

    @GET("/track/layer/play")
    suspend fun playLayer(@Query("layerId") layerId: String): Response<Any>

    @POST("/track/image")
    suspend fun uploadTrackImage(@Body requestBody: Any): Response<Any>
}