from django.urls import path

from .views import PlaylistView

urlpatterns = [
    path("playlist/", PlaylistView.as_view(), name="playlist"),
    path("playlist/<str:playlist_id>/", PlaylistView.as_view(), name="playlist-detail"),
]
