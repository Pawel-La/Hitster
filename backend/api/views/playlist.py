import random

from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from api.constants import (
    BACKUP_PLAYLIST_ID,
    DEFAULT_PLAYLIST_ID,
    DEFAULT_PLAYLIST_SIZE,
)
from api.serializers import PlaylistSongSerializer
from api.services.helpers import process_playlist_items
from api.services.maybe_fill_up_playlist import maybe_fill_up_playlist
from api.services.spotify_client import (
    SpotifyClient,
    SpotifyClientException,
)


class PlaylistView(APIView):
    """Returns the songs of a Spotify playlist in random order, fetched via the Spotify API."""

    def get(self, request, playlist_id=DEFAULT_PLAYLIST_ID):
        count = self._get_count(request)

        try:
            client = SpotifyClient()
        except ValueError as e:
            return Response(
                {"detail": str(e)},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

        try:
            playlist_items = client.fetch_playlist_items(playlist_id)
        except SpotifyClientException as e:
            return Response(
                {"detail": str(e)},
                status=status.HTTP_502_BAD_GATEWAY,
            )
        songs = process_playlist_items(playlist_items)

        try:
            backup_playlist_items = client.fetch_playlist_items(BACKUP_PLAYLIST_ID)
        except SpotifyClientException as e:
            return Response(
                {"detail": str(e)},
                status=status.HTTP_502_BAD_GATEWAY,
            )
        backup_songs = process_playlist_items(backup_playlist_items)

        songs = maybe_fill_up_playlist(songs, count, backup_songs)

        random.shuffle(songs)
        serializer = PlaylistSongSerializer(songs, many=True)
        return Response(serializer.data)

    @staticmethod
    def _get_count(request) -> int:
        try:
            return int(request.query_params.get("count", DEFAULT_PLAYLIST_SIZE))
        except (TypeError, ValueError):
            return DEFAULT_PLAYLIST_SIZE
