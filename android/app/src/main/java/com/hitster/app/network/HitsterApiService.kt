package com.hitster.app.network

import com.hitster.app.data.SongResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HitsterApiService {
    @GET("api/songs/")
    suspend fun getSongs(): List<SongResponse>

    @GET("api/songs/{id}/")
    suspend fun getSong(@Path("id") id: Int): SongResponse
}
