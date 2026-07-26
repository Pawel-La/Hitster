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
import androidx.compose.ui.unit.sp
import com.hitster.app.ui.screens.songplayer.GameMode
import com.hitster.app.ui.screens.songplayer.SongPlayerViewModel
import com.hitster.app.ui.components.BigButton

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
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Select Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        GameModeSelector(
            selectedMode = gameMode,
            onModeSelected = { viewModel.setGameMode(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Hard Mode Settings (Space preserved even in Easy mode)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            if (gameMode == GameMode.HARD) {
                NumericSettingsField(
                    label = "Seconds per song",
                    value = hardModeDuration,
                    onValueChange = { viewModel.setHardModeDuration(it) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        NumericSettingsField(
            label = "Number of players",
            value = playerCount,
            onValueChange = { viewModel.setPlayerCount(it) },
            textStyle = LocalTextStyle.current.copy(fontSize = 24.sp)
        )

        Spacer(modifier = Modifier.weight(1f))

        BigButton(
            text = "Play",
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}
