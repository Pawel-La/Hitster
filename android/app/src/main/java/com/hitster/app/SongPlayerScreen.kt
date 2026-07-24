package com.hitster.app

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

@Composable
fun SongPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: SongPlayerViewModel = viewModel()
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    val errorMessage by SpotifyManager.lastErrorMessage.collectAsState()
    var revealed by remember { mutableStateOf(false)}

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display error message if any
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
        BigButton(
            text = if (revealed) "Next" else "Skip",
            onClick = {
                revealed = false
                viewModel.reset()
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

        if (revealed) {
            RevealPopUp(
                year = 2017,
                artist = "Imagine Dragons",
                title = "Believer",
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