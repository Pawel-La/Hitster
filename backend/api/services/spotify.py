import os
import time
from pathlib import Path

import requests
from dotenv import load_dotenv


PROJECT_ROOT = Path(__file__).resolve().parents[2]
load_dotenv(PROJECT_ROOT / ".env")


_token_cache = {
    "token": None,
    "expires_at": 0
}


class SpotifyAPI:
    """Service for interacting with Spotify API"""
    
    BASE_AUTH_URL = "https://accounts.spotify.com/api/token"
    BASE_API_URL = "https://api.spotify.com/v1"
    
    def __init__(self) -> None:
        client_id = os.getenv("SPOTIFY_CLIENT_ID")
        client_secret = os.getenv("SPOTIFY_CLIENT_SECRET")
        
        if not client_id or not client_secret:
            raise ValueError("Please set SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET in your environment variables.")
        
        self.auth_data = {
            "grant_type": "client_credentials",
            "client_id": client_id,
            "client_secret": client_secret,
        }

    def query(self, endpoint: str, params: dict = None, headers: dict = None) -> dict:
        """Query Spotify API endpoint
        
        Args:
            endpoint (str): Spotify API endpoint (e.g., "/artists/{id}")
            params (dict): Query parameters (optional) 
            headers (dict): Headers for the request (optional)

        Returns:
            dict: JSON response from Spotify API
        """
        try:
            response = requests.get(f"{self.BASE_API_URL}{endpoint}", headers=headers, params=params)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to query Spotify API: {str(e)}")
    
    def get_access_token(self) -> str:
        """Get Spotify access token using client credentials flow"""
        if self._is_cached_token_valid():
            return _token_cache["token"]
        
        try:
            response = requests.post(
                self.BASE_AUTH_URL,
                data=self.auth_data,
                headers={"Content-Type": "application/x-www-form-urlencoded"}
            )
            response.raise_for_status()
            
            data = response.json()
            access_token = data.get("access_token")
            expires_in = data.get("expires_in")
            
            _token_cache["token"] = access_token
            _token_cache["expires_at"] = time.time() + expires_in - 60
            
            return access_token
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to get Spotify access token: {str(e)}")
        
    def _is_cached_token_valid(self) -> bool:
        return _token_cache["token"] and _token_cache["expires_at"] > time.time()
    
    def get_default_headers(self) -> dict:
        """Get default headers for Spotify API requests"""
        access_token = self.get_access_token()
        return {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json"
        }

    def get_artist(self, artist_id: str) -> dict:
        """Fetch artist information from Spotify API
        
        Args:
            artist_id (str): Spotify artist ID
            
        Returns:
            dict: Artist information including name, popularity, genres, etc.
        """
        try:
            headers = self.get_default_headers()
            return self.query(f"/artists/{artist_id}", headers=headers)
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to fetch artist data: {str(e)}")
    
    def search_artist(self, artist_name: str) -> dict:
        """Search for an artist by name
        
        Args:
            artist_name (str): Name of the artist to search
            
        Returns:
            dict: Search results
        """
        try:
            headers = self.get_default_headers()
            
            return self.query(
                "/search",
                params={
                    "q": artist_name,
                    "type": "artist",
                    "limit": 10
                },
                headers=headers
            )
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to search for artist: {str(e)}")


# Convenience functions
def get_spotify_artist(artist_id: str) -> dict:
    """Fetch artist information from Spotify"""
    spotify = SpotifyAPI()
    return spotify.get_artist(artist_id)


def search_spotify_artist(artist_name: str) -> dict:
    """Search for an artist on Spotify"""
    spotify = SpotifyAPI()
    return spotify.search_artist(artist_name)

if __name__ == "__main__":
    results = search_spotify_artist("Adele")
    artist_id = results["artists"]["items"][0]["id"]
    artist = get_spotify_artist(artist_id)
    print(artist["name"])
    print(artist["external_urls"]["spotify"])
