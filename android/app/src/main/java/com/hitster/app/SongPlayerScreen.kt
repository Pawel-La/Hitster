package com.hitster.app

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hitster.app.components.BigButton
import com.hitster.app.components.PlaybackControls
import com.hitster.app.components.RevealPopUp

@Composable
fun SongPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: SongPlayerViewModel = viewModel()
) {
    val isPlaying by viewModel.isPlaying.collectAsState()
    var revealed by remember { mutableStateOf(false)}

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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