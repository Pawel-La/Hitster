package com.hitster.app.data

import com.hitster.app.data.api.RetrofitClient
import android.util.Log

class SongRepository {
    suspend fun loadSongs(count: Int, playlistId: String? = null): List<Song> {
        return try {
            val response = if (playlistId != null) {
                RetrofitClient.instance.getPlaylistById(playlistId, count)
            } else {
                RetrofitClient.instance.getPlaylist(count)
            }
            response.map {
                Song(
                    title = it.name,
                    artist = it.artists.joinToString(", "),
                    year = it.year,
                    uri = it.uri
                )
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error loading songs from API", e)
            emptyList()
        }
    }

    suspend fun loadPlaylists(): List<Playlist> {
        return try {
            val response = RetrofitClient.instance.getCurrentUserPlaylists()
            response.map {
                Playlist(
                    id = it.id,
                    name = it.name,
                    imageUrl = it.images.firstOrNull()?.url
                )
            }
        } catch (e: Exception) {
            Log.e("SongRepository", "Error loading playlists from API", e)
            emptyList()
        }
    }
}
