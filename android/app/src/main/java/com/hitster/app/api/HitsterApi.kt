package com.hitster.app.api

import retrofit2.http.GET

interface HitsterApi {
    @GET("api/playlist/")
    suspend fun getPlaylist(): List<PlaylistSongResponse>
}

data class PlaylistSongResponse(
    val uri: String,
    val name: String,
    val artists: List<String>,
    val year: Int,
)
