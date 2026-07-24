from rest_framework import generics, status
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Song
from .serializers import PlaylistSongSerializer, SongSerializer
from .services.spotify_client import (
    SpotifyClient,
    SpotifyClientException,
    process_playlist_items,
)

DEFAULT_PLAYLIST_ID = "5GsuH4JNT7uiPGSKArlteE"


class HealthView(APIView):
    def get(self, request):
        return Response({"status": "ok", "message": "Hitster backend is running."})


class PlaylistView(APIView):
    """Returns the songs of a Spotify playlist fetched via the Spotify API."""

    def get(self, request):
        playlist_id = request.query_params.get("playlist_id", DEFAULT_PLAYLIST_ID)
        try:
            client = SpotifyClient()
            playlist_items = client.fetch_playlist_items(playlist_id)
            songs = process_playlist_items(playlist_items["items"])
        except ValueError as e:
            # Missing Spotify credentials / configuration.
            return Response(
                {"detail": str(e)},
                status=status.HTTP_500_INTERNAL_SERVER_ERROR,
            )
        except SpotifyClientException as e:
            return Response(
                {"detail": str(e)},
                status=status.HTTP_502_BAD_GATEWAY,
            )

        serializer = PlaylistSongSerializer(songs, many=True)
        return Response(serializer.data)


class SongListCreateView(generics.ListCreateAPIView):
    queryset = Song.objects.all().order_by("-created_at")
    serializer_class = SongSerializer


class SongDetailView(generics.RetrieveDestroyAPIView):
    queryset = Song.objects.all()
    serializer_class = SongSerializer
