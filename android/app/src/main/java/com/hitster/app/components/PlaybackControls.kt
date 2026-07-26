package com.hitster.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class PlaybackControlState(
    val isPlaying: Boolean = false,
    val isRewindEnabled: Boolean = true,
    val isForwardEnabled: Boolean = true,
    val isPlayPauseEnabled: Boolean = true,
    val isReplayEnabled: Boolean = true
)

@Composable
fun PlaybackControls(
    state: PlaybackControlState,
    onPlayPauseToggle: () -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val disabledColor = Color.Gray
    val enabledColor = Color.White

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .background(
                Color(0xFF381E72),
                RoundedCornerShape(50.dp)
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(
                onClick = onReplay,
                enabled = state.isReplayEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Replay",
                    modifier = Modifier.size(32.dp),
                    tint = if (state.isReplayEnabled) enabledColor else disabledColor
                )
            }
        }

        Row(
            modifier = Modifier.weight(3f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onRewind,
                enabled = state.isRewindEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Rewind",
                    modifier = Modifier.size(40.dp),
                    tint = if (state.isRewindEnabled) enabledColor else disabledColor
                )
            }

            // adjust this width to bring them even closer or further apart
            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = onPlayPauseToggle,
                enabled = state.isPlayPauseEnabled
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(160.dp),
                    tint = if (state.isPlayPauseEnabled) enabledColor else disabledColor
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = onForward,
                enabled = state.isForwardEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Forward",
                    modifier = Modifier.size(40.dp),
                    tint = if (state.isForwardEnabled) enabledColor else disabledColor
                )
            }
        }

        // empty space on far right to balance the Replay button
        Spacer(modifier = Modifier.weight(1f))
    }
}