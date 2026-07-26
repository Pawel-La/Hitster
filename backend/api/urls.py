from django.urls import path

from .views.current_user_playlists import CurrentUserPlaylistsView
from .views.playlist import PlaylistView

urlpatterns = [
    path("playlist/", PlaylistView.as_view(), name="playlist"),
    path("playlist/<str:playlist_id>/", PlaylistView.as_view(), name="playlist-detail"),
    path("current_user_playlists/", CurrentUserPlaylistsView.as_view(), name="current-user-playlists"),
]
