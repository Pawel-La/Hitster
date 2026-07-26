package com.hitster.app.data

import com.hitster.app.api.RetrofitClient
import com.hitster.app.model.Song
import android.util.Log

class SongRepository {
    suspend fun loadSongs(count: Int): List<Song> {
        return try {
            val response = RetrofitClient.instance.getPlaylist(count)
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
}
