import random

from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from .serializers import PlaylistSongSerializer
from .services.spotify_client import (
    SpotifyClient,
    SpotifyClientException,
    process_playlist_items,
)

DEFAULT_PLAYLIST_ID = "5GsuH4JNT7uiPGSKArlteE"

class PlaylistView(APIView):
    """Returns the songs of a Spotify playlist in random order, fetched via the Spotify API."""

    def get(self, request, playlist_id=None):
        if playlist_id is None:
            playlist_id = request.query_params.get("playlist_id", DEFAULT_PLAYLIST_ID)

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
        random.shuffle(songs)
        serializer = PlaylistSongSerializer(songs, many=True)
        return Response(serializer.data)
