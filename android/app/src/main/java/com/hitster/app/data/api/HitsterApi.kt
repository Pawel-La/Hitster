package com.hitster.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HitsterApi {
    @GET("api/playlist/")
    suspend fun getPlaylist(@Query("count") count: Int? = null): List<PlaylistSongResponse>

    @GET("api/playlist/{playlist_id}/")
    suspend fun getPlaylistById(
        @Path("playlist_id") playlistId: String,
        @Query("count") count: Int? = null
    ): List<PlaylistSongResponse>

    @GET("api/current_user_playlists/")
    suspend fun getCurrentUserPlaylists(): List<PlaylistResponse>
}

data class PlaylistSongResponse(
    val uri: String,
    val name: String,
    val artists: List<String>,
    val year: Int,
)

data class PlaylistResponse(
    val id: String,
    val name: String,
    val images: List<PlaylistImageResponse>
)

data class PlaylistImageResponse(
    val url: String,
    val height: Int?,
    val width: Int?
)
