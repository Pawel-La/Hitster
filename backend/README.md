# Hitster Backend

A minimal Django backend for the Hitster app. It exposes a small REST API for fetching songs from a Spotify playlist via the Spotify Web API.

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

## Environment variables

Create a `.env` file in the backend root. The Spotify integration requires:

```bash
SPOTIFY_CLIENT_ID=your_client_id
SPOTIFY_CLIENT_SECRET=your_client_secret
SPOTIFY_REFRESH_TOKEN=your_refresh_token
REDIRECT_URI=your_redirect_uri
```

`SPOTIFY_CLIENT_ID` and `SPOTIFY_CLIENT_SECRET` come from your app in the
[Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
`REDIRECT_URI` must match one of the redirect URIs registered for that app and is only used when generating a refresh token (see below).

### Generating a Spotify refresh token

If `SPOTIFY_REFRESH_TOKEN` is not set, run the helper script to obtain one. It opens a browser for authorization and prints the refresh token to add to your `.env`:

```bash
python -m api.services.get_spotify_refresh_token
```

## API

- `GET /api/health/` — returns a simple health check JSON response
  (`{"status": "ok", "message": "..."}`).
- `GET /api/playlist/` — returns the songs of the default Spotify playlist.
  Pass `?playlist_id=<id>` to fetch a different playlist.
- `GET /api/playlist/<playlist_id>/` — returns the songs of the given Spotify
  playlist.

### Playlist response

Each song fetched from Spotify is serialized as:

```json
{
  "uri": "spotify:track:...",
  "name": "Song title",
  "artists": ["Artist one", "Artist two"],
  "year": 1999
}
```

## Android integration

Use `Retrofit` and `Moshi` or `kotlinx.serialization` from the Android app.
Example with Retrofit:

```kotlin
interface HitsterApi {
    @GET("api/health/")
    suspend fun health(): HealthResponse

    @GET("api/playlist/")
    suspend fun playlist(): List<PlaylistSongResponse>
}

@Serializable
data class PlaylistSongResponse(
    val uri: String,
    val name: String,
    val artists: List<String>,
    val year: Int,
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

## Accessing the API from the deployed backend

```bash
curl https://hitstercopywannabe.onrender.com/api/playlist/
```
