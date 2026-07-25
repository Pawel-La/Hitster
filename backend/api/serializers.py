from rest_framework import serializers


class PlaylistSongSerializer(serializers.Serializer):
    """Serializes a Spotify playlist Song (see services.spotify_client.Song)."""

    uri = serializers.CharField()
    name = serializers.CharField()
    artists = serializers.ListField(child=serializers.CharField())
    year = serializers.IntegerField()


class ImageSerializer(serializers.Serializer):
    """Serializes a Spotify image."""

    url = serializers.CharField()
    height = serializers.IntegerField(allow_null=True)
    width = serializers.IntegerField(allow_null=True)


class PlaylistInfoSerializer(serializers.Serializer):
    """Serializes a Spotify PlaylistInfo (see services.helpers.PlaylistInfo)."""

    id = serializers.CharField()
    name = serializers.CharField()
    images = ImageSerializer(many=True)
