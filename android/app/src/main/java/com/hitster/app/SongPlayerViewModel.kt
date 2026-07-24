package com.hitster.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SongPlayerViewModel : ViewModel() {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var isTrackLoaded = false
    private val hardcodedTrackUri = "spotify:track:0pqnGHJpmpxLKifKRmU6WP" // Imagine Dragons - Believer

    private fun playTrack() {
        SpotifyManager.play(hardcodedTrackUri)
        _isPlaying.value = true
        isTrackLoaded = true
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
