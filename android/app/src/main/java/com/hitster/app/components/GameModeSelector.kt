package com.hitster.app.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hitster.app.ui.GameMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = selectedMode == GameMode.EASY,
            onClick = { onModeSelected(GameMode.EASY) },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
        ) {
            Text("Easy")
        }
        SegmentedButton(
            selected = selectedMode == GameMode.HARD,
            onClick = { onModeSelected(GameMode.HARD) },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
        ) {
            Text("Hard")
        }
    }
}
