package com.hitster.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SongPlayerViewModel : ViewModel() {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun rewind() {
        // TODO: Implement rewind 15s logic
    }

    fun fastForward() {
        // TODO: Implement forward 15s logic
    }

    fun replay() {
        // TODO: Implement restart logic
    }

    fun reset() {
        _isPlaying.value = false
    }
}