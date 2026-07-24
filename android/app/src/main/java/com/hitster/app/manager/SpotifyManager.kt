package com.hitster.app.manager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import androidx.activity.result.ActivityResultLauncher
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

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

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
                Log.e(TAG, "Full Auth Response: ${response.state}, ${response.code}")
                _lastErrorMessage.value = "Auth Failed: $error (Check SHA-1/Redirect URI)"
            }
            else -> {
                Log.d(TAG, "Auth flow cancelled or other: ${response.type}")
                _lastErrorMessage.value = "Auth cancelled"
            }
        }
    }

    /**
     * Step 3: Connect to the App Remote once authorized.
     */
    fun connect(context: Context) {
        if (spotifyAppRemote != null && spotifyAppRemote!!.isConnected) {
            Log.d(TAG, "Already connected to Spotify")
            return
        }

        Log.d(TAG, "Connecting to Spotify with Client ID: $CLIENT_ID")
        _lastErrorMessage.value = "Connecting..."

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                spotifyAppRemote = appRemote
                _isConnected.value = true
                _lastErrorMessage.value = null
                Log.d(TAG, "Connected to Spotify successfully!")
                
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Connected to Spotify!", Toast.LENGTH_SHORT).show()
                }

                // Log player state to verify session
                appRemote.playerApi.subscribeToPlayerState().setEventCallback { state ->
                    Log.d(TAG, "Player State: track=${state.track?.name}, isPaused=${state.isPaused}")
                }
            }

            override fun onFailure(throwable: Throwable) {
                val errorMsg = throwable.message ?: "Unknown error"
                Log.e(TAG, "Failed to connect to Spotify: $errorMsg", throwable)
                _isConnected.value = false
                _lastErrorMessage.value = "Connection Failed: $errorMsg"
            }
        })
    }

    fun disconnect() {
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
            spotifyAppRemote = null
            _isConnected.value = false
            Log.d(TAG, "Disconnected from Spotify")
        }
    }

    fun play(uri: String) {
        Log.d(TAG, "Attempting to play: $uri")
        if (spotifyAppRemote == null) {
            Log.e(TAG, "Cannot play: SpotifyAppRemote is null")
            _lastErrorMessage.value = "Playback Error: Not Connected"
            return
        }
        
        spotifyAppRemote?.playerApi?.play(uri)?.setResultCallback {
            Log.d(TAG, "Play command sent successfully for $uri")
        }?.setErrorCallback { error ->
            val msg = error.message ?: "Unknown playback error"
            Log.e(TAG, "Playback Error: $msg", error)
            _lastErrorMessage.value = "Playback Error: $msg"
            
            // If it's a "Spotify app not running" issue, suggest opening Spotify
            if (msg.contains("Spotify app not running", ignoreCase = true)) {
                 _lastErrorMessage.value = "Error: Open Spotify app first!"
            }
        }
    }

    fun pause() {
        Log.d(TAG, "Pausing playback")
        spotifyAppRemote?.playerApi?.pause()
    }

    fun resume() {
        Log.d(TAG, "Resuming playback")
        spotifyAppRemote?.playerApi?.resume()
    }

    fun seekTo(positionMs: Long) {
        Log.d(TAG, "Seeking to: $positionMs")
        spotifyAppRemote?.playerApi?.seekTo(positionMs)
    }

    fun seekToRelativePosition(milliseconds: Long) {
        Log.d(TAG, "Seeking relative: $milliseconds")
        spotifyAppRemote?.playerApi?.seekToRelativePosition(milliseconds)
    }
}
