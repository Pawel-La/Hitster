import base64
import os
import secrets
import webbrowser
from dataclasses import dataclass
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse

import requests
from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[2]
load_dotenv(PROJECT_ROOT / ".env")

BASE_AUTH_URL = "https://accounts.spotify.com/api/token"
BASE_API_URL = "https://api.spotify.com/v1"

captured = {}

class RefreshTokenRequestException(Exception):
    pass

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

@dataclass
class AuthData:
    client_id: str
    encoded_client_details: bytes
    redirect_uri: str

def get_spotify_refresh_token():
    auth_data = _get_auth_data_from_settings()
    auth_url = _get_request_user_authorization_url(auth_data)

    print("Opening browser for authorization...")
    webbrowser.open(auth_url)
    server = HTTPServer(("localhost", 3000), Handler)
    server.handle_request()

    return _make_request(captured["code"], auth_data)

def _get_auth_data_from_settings() -> AuthData:
    client_id = os.getenv("SPOTIFY_CLIENT_ID")
    client_secret = os.getenv("SPOTIFY_CLIENT_SECRET")
    redirect_uri = os.getenv("REDIRECT_URI")

    if not client_id or not client_secret or not redirect_uri:
        raise ValueError("Please set SPOTIFY_CLIENT_ID, SPOTIFY_CLIENT_SECRET and REDIRECT_URI in your /.env file.")

    encoded_client_details = f"{client_id}:{client_secret}".encode()
    return AuthData(
        client_id=client_id,
        encoded_client_details=encoded_client_details,
        redirect_uri=redirect_uri
    )

def _get_request_user_authorization_url(auth_data: AuthData) -> str:
    state = secrets.token_urlsafe(16)
    
    auth_url = (
        f"https://accounts.spotify.com/authorize"
        f"?response_type=code"
        f"&client_id={auth_data.client_id}"
        f"&scope=playlist-read-private playlist-read-collaborative"
        f"&redirect_uri={auth_data.redirect_uri}"
        f"&state={state}"
    )
    
    return auth_url

def _make_request(code: str, auth_data: AuthData) -> str:
    try:
        response = requests.post(
            BASE_AUTH_URL,
            data={
                "grant_type": "authorization_code",
                "code": code,
                "redirect_uri": auth_data.redirect_uri
            },
            headers={
                "content-type": "application/x-www-form-urlencoded",
                "Authorization": "Basic " + base64.b64encode(auth_data.encoded_client_details).decode()
            }
        )
        response.raise_for_status()
        data = response.json()
        return data.get("refresh_token")
    except requests.exceptions.RequestException as e:
        raise RefreshTokenRequestException(f"Failed to request access token: {e!s}")

if __name__ == "__main__":
    refresh_token = get_spotify_refresh_token()
    print(refresh_token)
