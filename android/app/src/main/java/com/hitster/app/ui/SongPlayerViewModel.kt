package com.hitster.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hitster.app.data.SongRepository
import com.hitster.app.manager.SpotifyManager
import com.hitster.app.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UiState {
    IDLE, LOADING, SUCCESS, ERROR
}

class SongPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository()
    
    private val _uiState = MutableStateFlow(UiState.IDLE)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSongIndex = MutableStateFlow(0)
    val currentSong: StateFlow<Song?> = combine(_currentSongIndex, _songs) { index, songsList ->
        if (songsList.isNotEmpty()) songsList[index % songsList.size] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var isTrackLoaded = false

    init {
        // We no longer fetch automatically on init
        // fetchSongs()
    }

    fun fetchSongs() {
        if (_uiState.value == UiState.LOADING || _uiState.value == UiState.SUCCESS) return
        
        viewModelScope.launch {
            _uiState.value = UiState.LOADING
            val fetchedSongs = repository.loadSongs()
            if (fetchedSongs.isNotEmpty()) {
                _songs.value = fetchedSongs
                _uiState.value = UiState.SUCCESS
            } else {
                _uiState.value = UiState.ERROR
            }
        }
    }

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
        val songsList = _songs.value
        if (songsList.isNotEmpty()) {
            _currentSongIndex.value = (_currentSongIndex.value + 1) % songsList.size
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

    fun hardReset() {
        reset()
        _songs.value = emptyList()
        _currentSongIndex.value = 0
        _uiState.value = UiState.IDLE
    }
}
