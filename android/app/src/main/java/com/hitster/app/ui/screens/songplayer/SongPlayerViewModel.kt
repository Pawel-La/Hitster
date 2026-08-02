package com.hitster.app.ui.screens.songplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hitster.app.data.SongRepository
import com.hitster.app.data.manager.SpotifyManager
import com.hitster.app.data.Song
import com.hitster.app.data.Playlist
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
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
    
    init {
        fetchPlaylists()
    }

    private val _uiState = MutableStateFlow(UiState.IDLE)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _songs = MutableStateFlow<List<Song>>(emptyList())

    private val _isPlaying = MutableStateFlow(false)

    private val _gameMode = MutableStateFlow(GameMode.EASY)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    private val _hardModeDurationSeconds = MutableStateFlow(30)
    val hardModeDurationSeconds: StateFlow<Int> = _hardModeDurationSeconds.asStateFlow()

    private val _playerCount = MutableStateFlow(10)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

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
            val targetCount = _playerCount.value * 10 + 20
            val fetchedSongs = repository.loadSongs(targetCount, _selectedPlaylist.value?.id)
            if (fetchedSongs.isNotEmpty()) {
                _songs.value = fetchedSongs
                _uiState.value = UiState.SUCCESS
            } else {
                _uiState.value = UiState.ERROR
            }
        }
    }

    fun fetchPlaylists() {
        viewModelScope.launch {
            val fetchedPlaylists = repository.loadPlaylists()
            _playlists.value = fetchedPlaylists
            if (_selectedPlaylist.value == null && fetchedPlaylists.isNotEmpty()) {
                _selectedPlaylist.value = fetchedPlaylists.first()
            }
        }
    }

    fun setSelectedPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
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
                SpotifyManager.resetState()
                SpotifyManager.resume()
                _isPlaying.value = true
                if (!_isRevealed.value) {
                    startTimer(isResume = true)
                }
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

    fun setPlayerCount(count: Int) {
        _playerCount.value = count
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

    private fun startTimer(isResume: Boolean = false) {
        if (_gameMode.value != GameMode.HARD || _isRevealed.value) return
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            // Wait for audio to actually start playing
            if (isResume) {
                while (!SpotifyManager.isActuallyPlaying.value) {
                    delay(100.milliseconds)
                }
            } else {
                // Ensure it's at the beginning if we just replayed/skipped
                while (!SpotifyManager.isActuallyPlaying.value || SpotifyManager.playbackPosition.value > 1000) {
                    delay(100.milliseconds)
                }
            }
            
            // Double check reveal state after waiting
            if (_isRevealed.value) return@launch

            while (_remainingMillis.value > 0) {
                delay(100.milliseconds)
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
        if (_isRevealed.value) return
        _isTimeUp.value = true
        SpotifyManager.pause()
        _isPlaying.value = false
    }
}
