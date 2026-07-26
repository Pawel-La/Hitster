from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from api.constants import BACKUP_PLAYLIST_ID
from api.serializers import PlaylistInfoSerializer
from api.services.helpers import process_playlist_infos
from api.services.spotify_client import (
    SpotifyClient,
    SpotifyClientException,
)


class CurrentUserPlaylistsView(APIView):
    """Returns the current user's playlists, fetched via the Spotify API.

    The backup playlist is left out: it is an internal pool of songs used to top
    up short playlists, not a playlist meant to be played on its own."""

    def get(self, request):
        try:
            client = SpotifyClient()
        except ValueError as e:
            return Response(
                {"detail": str(e)},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )

        try:
            playlists = client.fetch_current_user_playlists()
        except SpotifyClientException as e:
            return Response(
                {"detail": str(e)},
                status=status.HTTP_502_BAD_GATEWAY,
            )

        playlists = [
            playlist for playlist in playlists if playlist["id"] != BACKUP_PLAYLIST_ID
        ]

        playlist_infos = process_playlist_infos(playlists)
        serializer = PlaylistInfoSerializer(playlist_infos, many=True)
        return Response(serializer.data)
