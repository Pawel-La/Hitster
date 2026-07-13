package com.hitster.app.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "artist") val artist: String,
    @Json(name = "year") val year: Int,
    @Json(name = "created_at") val createdAt: String
)
