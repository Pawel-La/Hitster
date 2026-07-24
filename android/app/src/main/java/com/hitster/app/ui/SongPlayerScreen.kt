package com.hitster.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.zIndex
import com.hitster.app.components.BigButton
import com.hitster.app.components.PlaybackControls
import com.hitster.app.components.RevealPopUp
import com.hitster.app.manager.SpotifyManager
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun SongPlayerScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongPlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val errorMessage by SpotifyManager.lastErrorMessage.collectAsState()
    var revealed by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(text = "Exit Game?") },
            text = { Text(text = "Are you sure you want to exit? Your progress will be lost and the playlist will be reset.") },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    viewModel.hardReset()
                    onNavigateBack()
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
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
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading playlist...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            UiState.ERROR -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Failed to load playlist",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BigButton(text = "Retry", onClick = { viewModel.fetchSongs() })
                }
            }
            UiState.SUCCESS, UiState.IDLE -> {
                // Display error message if any (e.g. Spotify connection)
                errorMessage?.let {
                    androidx.compose.material3.Surface(
                        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .zIndex(1f) // Ensure it stays on top
                    ) {
                        androidx.compose.material3.Text(
                            text = it,
                            color = androidx.compose.ui.graphics.Color.Yellow,
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                
                if (uiState == UiState.SUCCESS) {
                    BigButton(
                        text = if (revealed) "Next" else "Skip",
                        onClick = {
                            revealed = false
                            viewModel.nextSong()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(0.5f)
                    )

                    BigButton(
                        text = "Reveal",
                        onClick = { revealed = true },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                    )

                    if (revealed && currentSong != null) {
                        RevealPopUp(
                            year = currentSong!!.year,
                            artist = currentSong!!.artist,
                            title = currentSong!!.title,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    PlaybackControls(
                        isPlaying = isPlaying,
                        onPlayPauseToggle = { viewModel.togglePlayPause() },
                        onRewind = { viewModel.rewind() },
                        onForward = { viewModel.fastForward() },
                        onReplay = { viewModel.replay() },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    )
                }
            }
        }
    }
}
