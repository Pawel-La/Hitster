package com.hitster.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hitster.app.components.PlaybackControlState
import com.hitster.app.data.SongRepository
import com.hitster.app.manager.SpotifyManager
import com.hitster.app.model.Song
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

enum class GameMode {
    EASY, HARD
}

class SongPlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SongRepository()
    
    private val _uiState = MutableStateFlow(UiState.IDLE)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _gameMode = MutableStateFlow(GameMode.EASY)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _hardModeDurationSeconds = MutableStateFlow(30)
    val hardModeDurationSeconds: StateFlow<Int> = _hardModeDurationSeconds.asStateFlow()

    private val _remainingMillis = MutableStateFlow(_hardModeDurationSeconds.value * 1000L)
    val remainingMillis: StateFlow<Long> = _remainingMillis.asStateFlow()

    private val _isTimeUp = MutableStateFlow(false)

    private val _isRevealed = MutableStateFlow(false)
    val isRevealed: StateFlow<Boolean> = _isRevealed.asStateFlow()

    val isSongFinished: StateFlow<Boolean> = SpotifyManager.isSongFinished

    val playbackControlState: StateFlow<PlaybackControlState> = combine(
        _isPlaying,
        _gameMode,
        _isRevealed,
        _isTimeUp,
        isSongFinished
    ) { playing: Boolean, mode: GameMode, revealed: Boolean, timeUp: Boolean, finished: Boolean ->
        val canRewindForward = !(mode == GameMode.HARD && !revealed) && !timeUp && !finished
        val canPlayPause = !timeUp && !finished
        val canReplay = true

        PlaybackControlState(
            isPlaying = playing,
            isRewindEnabled = canRewindForward,
            isForwardEnabled = canRewindForward,
            isPlayPauseEnabled = canPlayPause,
            isReplayEnabled = canReplay
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackControlState())

    private var timerJob: Job? = null
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSong: StateFlow<Song?> = combine(_currentSongIndex, _songs) { index, songsList ->
        if (songsList.isNotEmpty()) songsList[index % songsList.size] else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private var isTrackLoaded = false

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
            pauseTimer()
        } else {
            if (!isTrackLoaded) {
                playTrack()
                startTimer()
            } else {
                SpotifyManager.resume()
                _isPlaying.value = true
                startTimer()
            }
        }
    }

    fun nextSong() {
        val songsList = _songs.value
        if (songsList.isNotEmpty()) {
            _currentSongIndex.value = (_currentSongIndex.value + 1) % songsList.size
            _isTimeUp.value = false
            _isRevealed.value = false
            reset()
            playTrack()
            startTimer()
        }
    }

    fun rewind() {
        SpotifyManager.seekToRelativePosition(-15000)
    }

    fun fastForward() {
        SpotifyManager.seekToRelativePosition(15000)
    }

    fun replay() {
        _isTimeUp.value = false
        playTrack()
        if (_gameMode.value == GameMode.HARD && !_isRevealed.value) {
            resetTimer()
            startTimer()
        }
    }

    fun reset() {
        _isPlaying.value = false
        _isTimeUp.value = false
        _isRevealed.value = false
        SpotifyManager.pause()
        SpotifyManager.resetState()
        isTrackLoaded = false
        resetTimer()
    }

    fun hardReset() {
        reset()
        _songs.value = emptyList()
        _currentSongIndex.value = 0
        _uiState.value = UiState.IDLE
        resetTimer()
    }

    fun setGameMode(mode: GameMode) {
        _gameMode.value = mode
    }

    fun setHardModeDuration(seconds: Int) {
        _hardModeDurationSeconds.value = seconds
        if (_gameMode.value == GameMode.HARD) {
            _remainingMillis.value = seconds * 1000L
        }
    }

    fun onReveal() {
        pauseTimer()
        _isTimeUp.value = false
        _isRevealed.value = true
        if (!_isPlaying.value) {
            if (!isTrackLoaded) {
                playTrack()
            } else {
                SpotifyManager.resume()
                _isPlaying.value = true
            }
        }
    }

    private fun startTimer() {
        if (_gameMode.value != GameMode.HARD) return
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingMillis.value > 0) {
                delay(100L)
                val newValue = _remainingMillis.value - 100L
                _remainingMillis.value = if (newValue < 0) 0 else newValue
            }
            onTimeUp()
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
    }

    private fun resetTimer() {
        timerJob?.cancel()
        _remainingMillis.value = _hardModeDurationSeconds.value * 1000L
    }

    private fun onTimeUp() {
        _isTimeUp.value = true
        SpotifyManager.pause()
        _isPlaying.value = false
    }
}
