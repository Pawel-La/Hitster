# Hitster Backend

A minimal Django backend for the Hitster app.

## Setup

If you use the workspace root virtual environment:

```bash
cd /Hitster/backend
source ../.venv/bin/activate
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver 8000
```

Alternatively, if you create a backend-specific virtual environment:

```bash
cd /Hitster/backend
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver 8000
```

## API

- `GET /api/health/` — returns a simple health check JSON response.
- `GET /api/songs/` — returns a list of songs.
- `POST /api/songs/` — creates a new song.
- `GET /api/songs/<id>/` — returns a single song.
- `DELETE /api/songs/<id>/` — deletes a song.

## CORS

This backend enables CORS for all origins to support future web clients. Native Android clients do not require CORS, but the setting is useful if you also use a browser-based frontend later.

## Android integration

Use `Retrofit` and `Moshi` or `kotlinx.serialization` from the Android app. Example with Retrofit:

```kotlin
interface HitsterApi {
    @GET("api/health/")
    suspend fun health(): HealthResponse

    @GET("api/songs/")
    suspend fun songs(): List<SongResponse>

    @POST("api/songs/")
    suspend fun createSong(@Body request: CreateSongRequest): SongResponse
}

@Serializable
data class HealthResponse(val status: String, val message: String)

@Serializable
data class SongResponse(
    val id: Int,
    val title: String,
    val artist: String,
    val year: Int,
    val created_at: String,
)

@Serializable
data class CreateSongRequest(
    val title: String,
    val artist: String,
    val year: Int = 0,
)
```

Then build Retrofit with the deployed backend base URL, for example:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://your-backend.onrender.com/")
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .build()
```

Optionally add delete support to the Android API interface:

```kotlin
@DELETE("api/songs/{id}/")
suspend fun deleteSong(@Path("id") id: Int)
```

## Notes

- The Android frontend will be placed under `/android`.
- This backend uses SQLite for simple local development.
