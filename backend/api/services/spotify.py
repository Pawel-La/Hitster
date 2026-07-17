from http.server import BaseHTTPRequestHandler, HTTPServer
import os
import secrets
import time
from pathlib import Path
import base64
from urllib.parse import parse_qs, urlparse
import webbrowser

import requests
from dotenv import load_dotenv


PROJECT_ROOT = Path(__file__).resolve().parents[2]
load_dotenv(PROJECT_ROOT / ".env")


class SpotifyAPI:
    """Service for interacting with Spotify API"""
    
    BASE_AUTH_URL = "https://accounts.spotify.com/api/token"
    BASE_API_URL = "https://api.spotify.com/v1"
    REDIRECT_URI = "http://127.0.0.1:3000"
    
    def __init__(self) -> None:
        client_id = os.getenv("SPOTIFY_CLIENT_ID")
        client_secret = os.getenv("SPOTIFY_CLIENT_SECRET")
        
        if not client_id or not client_secret:
            raise ValueError("Please set SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET in your environment variables.")
        
        self.CLIENT_ID = client_id
        self.ENCODED_CLIENT_DETAILS = f"{client_id}:{client_secret}".encode("utf-8")
        self._token_cache = {
            "token": None,
            "expires_at": 0,
            "refresh_token": None
        }
    
    def get_access_token(self) -> str:
        if self._token_cache["token"] and self._token_cache["expires_at"] > time.time():
            print("Token in cache still active.")
            return self._token_cache["token"]
        
        if self._token_cache["refresh_token"]:
            print("Token in cache is inactive. Refreshing Token.")
            return self._refresh_token()
        
        print("Token in cache is inactive. Refreshing Token is invalid. Requesting new token")
        return self._get_new_access_token()
    
    def _refresh_token(self) -> str:
        try:
            response = requests.post(
                self.BASE_AUTH_URL,
                data={
                    "grant_type": "refresh_token",
                    "refresh_token": self._token_cache["refresh_token"],
                },
                headers={
                    "content-type": "application/x-www-form-urlencoded",
                    "Authorization": "Basic " + base64.b64encode(self.ENCODED_CLIENT_DETAILS).decode()
                }
            )
            response.raise_for_status()

            # around here add handling of invalid case ---> "invalid_grant" returned
            # (https://developer.spotify.com/documentation/web-api/tutorials/refreshing-tokens#:~:text=encoded%20client_id%3Aclient_secret%3E-,Example,-The%20following%20code)

            data = response.json()
            access_token = data.get("access_token")
            expires_in = data.get("expires_in")
            refresh_token = data.get("refresh_token", None)
            
            self._token_cache["token"] = access_token
            self._token_cache["expires_at"] = time.time() + expires_in - 60
            if refresh_token:
                self._token_cache["refresh_token"] = refresh_token
            return access_token
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to request access token: {str(e)}")
        
    def _get_new_access_token(self):
        auth_url = self._get_request_user_authorization_url()

        print("Opening browser for authorization...")
        webbrowser.open(auth_url)
        server = HTTPServer(("localhost", 3000), Handler)
        server.handle_request()
        print("Redirected back with code:", captured["code"])
        code = captured["code"]
        return self._request_new_access_token(code)

    def _get_request_user_authorization_url(self) -> str:
        state = secrets.token_urlsafe(16)
        
        auth_url = (
            f"https://accounts.spotify.com/authorize"
            f"?response_type=code"
            f"&client_id={self.CLIENT_ID}"
            f"&scope=user-read-private user-read-email"
            f"&redirect_uri={self.REDIRECT_URI}"
            f"&state={state}"
        )
        
        return auth_url

    def _request_new_access_token(self, code: str) -> str:
        try:
            response = requests.post(
                self.BASE_AUTH_URL,
                data={
                    "grant_type": "authorization_code",
                    "code": code,
                    "redirect_uri": self.REDIRECT_URI
                },
                headers={
                    "content-type": "application/x-www-form-urlencoded",
                    "Authorization": "Basic " + base64.b64encode(self.ENCODED_CLIENT_DETAILS).decode()
                }
            )
            response.raise_for_status()

            data = response.json()
            access_token = data.get("access_token")
            expires_in = data.get("expires_in")
            refresh_token = data.get("refresh_token")
            
            self._token_cache["token"] = access_token
            self._token_cache["expires_at"] = time.time() + expires_in - 60
            self._token_cache["refresh_token"] = refresh_token
            
            return access_token
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to request access token: {str(e)}")
    
    def get_playlist(self, playlist_id: str) -> dict:
        try:
            headers = self._get_default_headers()
            response = requests.get(f"{self.BASE_API_URL}/playlists/{playlist_id}/items", headers=headers)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            raise Exception(f"Failed to fetch playlist data: {str(e)}")
        
    def _get_default_headers(self) -> dict:
        access_token = self.get_access_token()
        return {
            "Authorization": f"Bearer {access_token}",
            "Content-Type": "application/json"
        }

captured = {}

class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        query = parse_qs(urlparse(self.path).query)
        captured["code"] = query.get("code", [None])[0]
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(b"<h1>You can close this tab.</h1>")

    def log_message(self, format, *args):
        pass  # silence default logging

if __name__ == "__main__":
    spotify = SpotifyAPI()
    token = spotify.get_access_token()
    print(token)
    print(spotify._token_cache)
    # spotify_playlist_id = "5GsuH4JNT7uiPGSKArlteE"
    # items = spotify.get_playlist(spotify_playlist_id)
