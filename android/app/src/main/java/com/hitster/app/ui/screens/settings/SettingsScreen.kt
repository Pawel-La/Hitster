package com.hitster.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.hitster.app.ui.screens.songplayer.GameMode
import com.hitster.app.ui.screens.songplayer.SongPlayerViewModel
import com.hitster.app.ui.components.BigButton

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongPlayerViewModel
) {
    val gameMode by viewModel.gameMode.collectAsState()
    val hardModeDuration by viewModel.hardModeDurationSeconds.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchPlaylists()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Select Mode",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            GameModeSelector(
                selectedMode = gameMode,
                onModeSelected = { viewModel.setGameMode(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            if (gameMode == GameMode.HARD) {
                Spacer(modifier = Modifier.height(32.dp))
                NumericSettingsField(
                    label = "Seconds per song",
                    value = hardModeDuration,
                    onValueChange = { viewModel.setHardModeDuration(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            NumericSettingsField(
                label = "Number of players",
                value = playerCount,
                onValueChange = { viewModel.setPlayerCount(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            val playlists by viewModel.playlists.collectAsState()
            val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

            PlaylistSelector(
                playlists = playlists,
                selectedPlaylist = selectedPlaylist,
                onPlaylistSelected = { viewModel.setSelectedPlaylist(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        BigButton(
            text = "Play",
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}
