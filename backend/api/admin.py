from django.contrib import admin
from .models import Song


@admin.register(Song)
class SongAdmin(admin.ModelAdmin):
    list_display = ("id", "title", "artist", "year", "created_at")
    list_filter = ("artist", "year")
    search_fields = ("title", "artist")
