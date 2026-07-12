from rest_framework import generics
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Song
from .serializers import SongSerializer


class HealthView(APIView):
    def get(self, request):
        return Response({"status": "ok", "message": "Hitster backend is running."})


class SongListCreateView(generics.ListCreateAPIView):
    queryset = Song.objects.all().order_by("-created_at")
    serializer_class = SongSerializer


class SongDetailView(generics.RetrieveDestroyAPIView):
    queryset = Song.objects.all()
    serializer_class = SongSerializer
