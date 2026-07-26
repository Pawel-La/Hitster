from rest_framework import status
from rest_framework.response import Response
from rest_framework.views import APIView

from api.serializers import PlaylistInfoSerializer
from api.services.helpers import process_playlist_infos
from api.services.spotify_client import (
    SpotifyClient,
    SpotifyClientException,
)


class CurrentUserPlaylistsView(APIView):
    """Returns the current user's playlists, fetched via the Spotify API."""

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

        playlist_infos = process_playlist_infos(playlists)
        serializer = PlaylistInfoSerializer(playlist_infos, many=True)
        return Response(serializer.data)
