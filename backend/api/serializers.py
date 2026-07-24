from rest_framework import serializers

from .models import Song


class SongSerializer(serializers.ModelSerializer):
    class Meta:
        model = Song
        fields = ["id", "title", "artist", "year", "created_at"]
        read_only_fields = ["id", "created_at"]


class PlaylistSongSerializer(serializers.Serializer):
    """Serializes a Spotify playlist Song (see services.spotify_client.Song)."""

    uri = serializers.CharField()
    name = serializers.CharField()
    artists = serializers.ListField(child=serializers.CharField())
    year = serializers.IntegerField()
