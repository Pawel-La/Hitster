package com.hitster.app.data.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import androidx.activity.result.ActivityResultLauncher
import com.spotify.protocol.types.Track
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object SpotifyManager {
    private const val TAG = "SpotifyManager"
    
    private const val CLIENT_ID = "c5785ed2d472454da01d05b9f5cc857d"
    private const val REDIRECT_URI = "http://127.0.0.1:3000"

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var requestedUri: String? = null
    private var isRequestedTrackStarted = false
    
    private var lastLoggedUri: String? = null
    private var lastLoggedPausedState: Boolean? = null

    private val _isSongFinished = MutableStateFlow(false)
    val isSongFinished: StateFlow<Boolean> = _isSongFinished

    private val _isActuallyPlaying = MutableStateFlow(false)
    val isActuallyPlaying: StateFlow<Boolean> = _isActuallyPlaying

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    /**
     * Step 1: Request Authorization from the user.
     */
    fun authorize(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        Log.d(TAG, "Requesting Spotify Authorization")
        val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        builder.setScopes(arrayOf("app-remote-control", "streaming", "playlist-read-private"))
        val request = builder.build()
        val intent = AuthorizationClient.createLoginActivityIntent(activity, request)
        launcher.launch(intent)
    }

    /**
     * Step 2: Handle the result from the authorization activity.
     */
    fun handleAuthResult(resultCode: Int, data: Intent?, context: Context) {
        val response = AuthorizationClient.getResponse(resultCode, data)

        when (response.type) {
            AuthorizationResponse.Type.TOKEN -> {
                Log.d(TAG, "Auth successful, token received. Connecting to App Remote...")
                connect(context)
            }
            AuthorizationResponse.Type.ERROR -> {
                val error = response.error ?: "Unknown auth error"
                Log.e(TAG, "Auth error: $error")
                _lastErrorMessage.value = "Auth Failed: $error (Check SHA-1/Redirect URI)"
            }
            else -> {
                Log.d(TAG, "Auth flow cancelled or other: ${response.type}")
                _lastErrorMessage.value = "Auth cancelled"
            }
        }
    }

    fun isConnected(): Boolean = spotifyAppRemote?.isConnected == true

    /**
     * Step 3: Connect to the App Remote once authorized.
     */
    fun connect(context: Context) {
        if (isConnected()) return

        _lastErrorMessage.value = "Connecting..."

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context.applicationContext, connectionParams, createConnectionListener(context.applicationContext))
    }

    private fun createConnectionListener(context: Context) = object : Connector.ConnectionListener {
        override fun onConnected(appRemote: SpotifyAppRemote) {
            spotifyAppRemote = appRemote
            _lastErrorMessage.value = null
            Log.d(TAG, "Connected to Spotify successfully!")
            
            showToast(context)
            setupPlayerStateSubscription(appRemote)
        }

        override fun onFailure(throwable: Throwable) {
            val errorMsg = throwable.message ?: "Unknown error"
            Log.e(TAG, "Failed to connect to Spotify: $errorMsg", throwable)
            _lastErrorMessage.value = "Connection Failed: $errorMsg"
        }
    }

    private fun setupPlayerStateSubscription(appRemote: SpotifyAppRemote) {
        appRemote.playerApi.subscribeToPlayerState().setEventCallback { state ->
            val track = state.track
            
            if (track?.uri != lastLoggedUri || state.isPaused != lastLoggedPausedState) {
                Log.d(TAG, "Player State Change: track=${track?.name}, uri=${track?.uri}, isPaused=${state.isPaused}")
                lastLoggedUri = track?.uri
                lastLoggedPausedState = state.isPaused
            }
            
            _isActuallyPlaying.value = !state.isPaused && track != null
            _playbackPosition.value = state.playbackPosition

            if (track != null && requestedUri != null) {
                handleAutoplayDetection(track, state.isPaused)
            }
        }
    }

    private fun handleAutoplayDetection(track: Track, isPaused: Boolean) {
        if (track.uri == requestedUri) {
            isRequestedTrackStarted = true
            _isSongFinished.value = false
        } else if (isRequestedTrackStarted && !isPaused) {
            pause()
            isRequestedTrackStarted = false
            _isSongFinished.value = true
            Log.d(TAG, "Autoplay detected and paused.")
        }
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote = null
            resetState()
            _lastErrorMessage.value = null
            Log.d(TAG, "Disconnected from Spotify")
        }
    }

    fun resetState() {
        requestedUri = null
        isRequestedTrackStarted = false
        _isSongFinished.value = false
        _isActuallyPlaying.value = false
    }

    fun play(uri: String) {
        Log.d(TAG, "Attempting to play: $uri")
        val remote = spotifyAppRemote
        if (remote == null) {
            Log.e(TAG, "Cannot play: SpotifyAppRemote is null")
            _lastErrorMessage.value = "Playback Error: Not Connected"
            return
        }
        
        prepareForNewTrack(uri)
        
        remote.playerApi.play(uri)
            .setResultCallback { Log.d(TAG, "Play command sent successfully for $uri") }
            .setErrorCallback { error -> handlePlaybackError(error) }
    }

    private fun prepareForNewTrack(uri: String) {
        requestedUri = uri
        isRequestedTrackStarted = false
        _isSongFinished.value = false
    }

    private fun handlePlaybackError(error: Throwable) {
        val msg = error.message ?: "Unknown playback error"
        Log.e(TAG, "Playback Error: $msg", error)
        
        _lastErrorMessage.value = if (msg.contains("Spotify app not running", ignoreCase = true)) {
            "Error: Open Spotify app first!"
        } else {
            "Playback Error: $msg"
        }
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun seekToRelativePosition(milliseconds: Long) {
        spotifyAppRemote?.playerApi?.seekToRelativePosition(milliseconds)
    }

    private fun showToast(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(context, "Connected to Spotify!", Toast.LENGTH_SHORT).show()
        }
    }
}
