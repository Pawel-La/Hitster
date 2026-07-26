package com.hitster.app.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface HitsterApi {
    @GET("api/playlist/")
    suspend fun getPlaylist(@Query("count") count: Int? = null): List<PlaylistSongResponse>
}

data class PlaylistSongResponse(
    val uri: String,
    val name: String,
    val artists: List<String>,
    val year: Int,
)
