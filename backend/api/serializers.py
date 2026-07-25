from rest_framework import serializers


class PlaylistSongSerializer(serializers.Serializer):
    """Serializes a Spotify playlist Song (see services.spotify_client.Song)."""

    uri = serializers.CharField()
    name = serializers.CharField()
    artists = serializers.ListField(child=serializers.CharField())
    year = serializers.IntegerField()
