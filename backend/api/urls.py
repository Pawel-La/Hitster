from django.urls import path
from .views import HealthView, SongDetailView, SongListCreateView

urlpatterns = [
    path("health/", HealthView.as_view(), name="health"),
    path("songs/", SongListCreateView.as_view(), name="song-list-create"),
    path("songs/<int:pk>/", SongDetailView.as_view(), name="song-detail"),
]
