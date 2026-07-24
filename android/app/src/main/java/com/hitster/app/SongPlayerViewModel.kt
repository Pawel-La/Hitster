package com.hitster.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SongPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository(application)
    private val songs = repository.loadSongs()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSongIndex = MutableStateFlow(0)
    val currentSong: StateFlow<Song?> = _currentSongIndex.map { index ->
        if (songs.isNotEmpty()) songs[index] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var isTrackLoaded = false

    private fun playTrack() {
        currentSong.value?.let { song ->
            SpotifyManager.play(song.uri)
            _isPlaying.value = true
            isTrackLoaded = true
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            SpotifyManager.pause()
            _isPlaying.value = false
        } else {
            if (!isTrackLoaded) {
                playTrack()
            } else {
                SpotifyManager.resume()
                _isPlaying.value = true
            }
        }
    }

    fun nextSong() {
        if (songs.isNotEmpty()) {
            _currentSongIndex.value = (_currentSongIndex.value + 1) % songs.size
            reset()
        }
    }

    fun rewind() {
        SpotifyManager.seekToRelativePosition(-15000)
    }

    fun fastForward() {
        SpotifyManager.seekToRelativePosition(15000)
    }

    fun replay() {
        if (!isTrackLoaded) {
            playTrack()
        } else {
            SpotifyManager.seekTo(0)
            SpotifyManager.resume()
            _isPlaying.value = true
        }
    }

    fun reset() {
        _isPlaying.value = false
        SpotifyManager.pause()
        isTrackLoaded = false
    }
}
