import base64
import os
import time
from dataclasses import dataclass
from pathlib import Path

import requests
from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[2]
load_dotenv(PROJECT_ROOT / ".env")


_token_cache = {
    "token": None,
    "expires_at": 0
}

class SpotifyClientException(Exception):
    pass

class SpotifyClient:
    """Service for interacting with Spotify API"""
    def __init__(self) -> None:
        client_id = os.getenv("SPOTIFY_CLIENT_ID")
        client_secret = os.getenv("SPOTIFY_CLIENT_SECRET")
        refresh_token = os.getenv("SPOTIFY_REFRESH_TOKEN")
        
        if not client_id or not client_secret:
            raise ValueError("Please set SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET in your /.env file.")
        if not refresh_token:
            raise ValueError("SPOTIFY_REFRESH_TOKEN is not set. Run `get_spotify_refresh_token` to get new refresh token.")
        
        self.CLIENT_CREDENTIALS = f"{client_id}:{client_secret}".encode()
        self.REFRESH_TOKEN = refresh_token

        self.BASE_AUTH_URL = "https://accounts.spotify.com/api/token"
        self.BASE_API_URL = "https://api.spotify.com/v1"
    
    def fetch_playlist_items(self, playlist_id: str) -> list[dict]:
        try:
            headers = self._get_default_headers()
            url = f"{self.BASE_API_URL}/playlists/{playlist_id}/items?limit=50"
            items: list[dict] = []
            while url:
                response = requests.get(url, headers=headers)
                response.raise_for_status()
                data = response.json()
                items.extend(data["items"])
                url = data["next"]
            return items
        except requests.exceptions.RequestException as e:
            raise SpotifyClientException(f"Failed to fetch playlist data: {e!s}")
        
    def _get_default_headers(self) -> dict:
        return {
            "Authorization": f"Bearer {self._get_access_token()}",
            "Content-Type": "application/json"
        }
    
    def _get_access_token(self) -> str:
        if _token_cache["token"] and _token_cache["expires_at"] > time.time():
            print("Token in cache still active.")
            return _token_cache["token"]
        
        print("Token in cache is inactive. Refreshing Token.")
        return self._refresh_token()
            
    def _refresh_token(self) -> str:
        try:
            response = requests.post(
                self.BASE_AUTH_URL,
                data={
                    "grant_type": "refresh_token",
                    "refresh_token": self.REFRESH_TOKEN,
                },
                headers={
                    "content-type": "application/x-www-form-urlencoded",
                    "Authorization": "Basic " + base64.b64encode(self.CLIENT_CREDENTIALS).decode()
                }
            )
            response.raise_for_status()
            data = response.json()
            # around here add handling of invalid case ---> "invalid_grant" returned
            # (https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens#:~:text=encoded%20client_id%3Aclient_secret%3E-,Example,-The%20following%20code)

            self._save_token_details(data)

            return data.get("access_token")
        except requests.exceptions.RequestException as e:
            raise SpotifyClientException(f"Failed to refresh access token: {e!s}")
    
    def _save_token_details(self, data: dict) -> None:
        _token_cache["token"] = data.get("access_token")
        _token_cache["expires_at"] = time.time() + data.get("expires_in") - 60


@dataclass
class Song:
    uri: str
    name: str
    artists: list[str]
    year: int

    def __str__(self) -> str:
        return f"""
        URI: {self.uri}
        SONG NAME: {self.name}
        ARTISTS: {", ".join([artist for artist in self.artists])}
        YEAR: {self.year}
        """

def process_playlist_items(items: list[dict]) -> list[Song]:
    return [
        Song(
            uri=item["item"]["uri"],
            name=item["item"]["name"],
            artists=[artist["name"] for artist in item["item"]["artists"]],
            year=int(get_year_from_release_date(item["item"]["album"]["release_date"]))
        ) 
        for item in items
    ]

def get_year_from_release_date(date: str):
    return date.split('-')[0]
