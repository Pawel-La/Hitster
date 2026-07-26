import os
from pathlib import Path

from dotenv import load_dotenv

PROJECT_ROOT = Path(__file__).resolve().parents[1]
load_dotenv(PROJECT_ROOT / ".env")

DEFAULT_PLAYLIST_ID = os.getenv("DEFAULT_PLAYLIST_ID")
BACKUP_PLAYLIST_ID = os.getenv("BACKUP_PLAYLIST_ID")
DEFAULT_PLAYLIST_SIZE = os.getenv("DEFAULT_PLAYLIST_SIZE")

if not DEFAULT_PLAYLIST_ID or not BACKUP_PLAYLIST_ID or not DEFAULT_PLAYLIST_SIZE:
    raise ValueError(
        "Please set DEFAULT_PLAYLIST_ID, BACKUP_PLAYLIST_ID and "
        "DEFAULT_PLAYLIST_SIZE in your /.env file."
    )

DEFAULT_PLAYLIST_SIZE = int(DEFAULT_PLAYLIST_SIZE)
