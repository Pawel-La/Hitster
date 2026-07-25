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

- `GET /api/playlist/` — returns the songs of the default Spotify playlist.
- `GET /api/playlist/<playlist_id>/` — returns the songs of the given Spotify playlist.
- `GET /api/current_user_playlists/` — returns the current user's playlists (only those they own), suitable for choosing a playlist to play.

Songs are always returned in random order.

### Query parameters

- `count` — target number of songs. If the requested playlist has fewer than `count` songs, it is topped up with songs from a backup playlist to reach `count`; if it already has at least `count`, it is returned as-is. Example: `GET /api/playlist/?count=100`.

### Fill-up behavior

When a playlist is shorter than `count`, songs are bucketed by release-year period, and backup songs matching each period are added so the periods keep roughly the same proportions as the original playlist.

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

### Current user playlists response

Each playlist is serialized as:

```json
{
  "id": "41241241412412414",
  "name": "Playlist name",
  "images": [
    { "url": "https://i.scdn.co/image/...", "height": 640, "width": 640 }
  ]
}
```

Only playlists the user owns or collaborates on are returned; followed playlists are excluded. Image `height`/`width` may be `null` when Spotify does not provide them.

## Android integration

Use `Retrofit` and `Moshi` or `kotlinx.serialization` from the Android app.
Example with Retrofit:

```kotlin
interface HitsterApi {
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

## Notes

- The Android frontend will be placed under `/android`.
- This backend uses SQLite for simple local development.

## Accessing the API from the deployed backend

```bash
curl https://hitstercopywannabe.onrender.com/api/playlist/
curl https://hitstercopywannabe.onrender.com/api/playlist/[playlist_id]/
curl https://hitstercopywannabe.onrender.com/api/current_user_playlists/
```
