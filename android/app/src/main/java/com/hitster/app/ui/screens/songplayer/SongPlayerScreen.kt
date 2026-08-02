package com.hitster.app.ui.screens.songplayer

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hitster.app.ui.components.BigButton
import com.hitster.app.ui.components.SpotifyErrorDisplay
import com.hitster.app.ui.components.LoadingScreen
import com.hitster.app.ui.components.LoadingErrorScreen
import com.hitster.app.data.manager.SpotifyManager
import androidx.activity.compose.BackHandler

import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

@Composable
fun SongPlayerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongPlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val gameMode by viewModel.gameMode.collectAsState()
    val remainingMillis by viewModel.remainingMillis.collectAsState()
    val hardModeDurationSeconds by viewModel.hardModeDurationSeconds.collectAsState()
    val isRevealed by viewModel.isRevealed.collectAsState()
    val playbackControlState by viewModel.playbackControlState.collectAsState()

    val errorMessage by SpotifyManager.lastErrorMessage.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitGameDialog(
            onConfirm = {
                showExitDialog = false
                viewModel.hardReset()
                onNavigateBack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSongs()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (uiState) {
            UiState.LOADING -> {
                LoadingScreen(modifier = Modifier.align(Alignment.Center))
            }
            UiState.ERROR -> {
                LoadingErrorScreen(
                    onRetry = { viewModel.fetchSongs() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            UiState.SUCCESS, UiState.IDLE -> {
                errorMessage?.let {
                    SpotifyErrorDisplay(
                        errorMessage = it,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                if (uiState == UiState.SUCCESS) {
                    if (gameMode == GameMode.HARD && !isRevealed) {
                        CountdownTimer(
                            remainingMillis = remainingMillis,
                            totalDurationMillis = hardModeDurationSeconds * 1000L,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        )
                    }

                    BigButton(
                        text = if (isRevealed) "Next" else "Skip",
                        onClick = {
                            viewModel.nextSong()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(0.5f)
                    )

                    if (!isRevealed) {
                        BigButton(
                            text = "Reveal",
                            onClick = {
                                viewModel.onReveal()
                            },
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                        )
                    }

                    if (isRevealed && currentSong != null) {
                        RevealPopUp(
                            year = currentSong!!.year,
                            artist = currentSong!!.artist,
                            title = currentSong!!.title,
                            isLandscape = isLandscape,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    PlaybackControls(
                        state = playbackControlState,
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onRewind = { viewModel.rewind() },
                        onForward = { viewModel.fastForward() },
                        onReplay = { viewModel.replay() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (isLandscape) 8.dp else 32.dp)
                            .fillMaxWidth(if (isLandscape) 0.6f else 1f)
                    )
                }
            }
        }
    }
}
