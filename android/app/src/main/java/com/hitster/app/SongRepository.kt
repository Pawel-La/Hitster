package com.hitster.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class SongRepository(private val context: Context) {
    fun loadSongs(): List<Song> {
        return try {
            val inputStream = context.assets.open("songs.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Song>>() {}.type
            Gson().fromJson(reader, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
