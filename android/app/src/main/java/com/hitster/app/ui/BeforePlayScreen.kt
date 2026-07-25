package com.hitster.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hitster.app.components.BigButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeforePlayScreen(
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SongPlayerViewModel
) {
    val gameMode by viewModel.gameMode.collectAsState()
    val hardModeDuration by viewModel.hardModeDurationSeconds.collectAsState()
    val focusManager = LocalFocusManager.current
    
    // Local state for the text field to handle focus and editing behavior
    var textFieldValue by remember { 
        mutableStateOf(TextFieldValue(hardModeDuration.toString())) 
    }
    
    // Sync from ViewModel only when NOT focused and value is different
    var isFocused by remember { mutableStateOf(false) }
    
    LaunchedEffect(hardModeDuration) {
        if (!isFocused && textFieldValue.text != hardModeDuration.toString()) {
            textFieldValue = TextFieldValue(hardModeDuration.toString())
        }
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
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Select Mode",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SegmentedButton(
                selected = gameMode == GameMode.EASY,
                onClick = { viewModel.setGameMode(GameMode.EASY) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Easy")
            }
            SegmentedButton(
                selected = gameMode == GameMode.HARD,
                onClick = { viewModel.setGameMode(GameMode.HARD) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Hard")
            }
        }

        if (gameMode == GameMode.HARD) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Seconds per song:",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Only allow digits
                    if (newValue.text.all { it.isDigit() }) {
                        textFieldValue = newValue
                        // Sync to ViewModel if it's a valid number
                        newValue.text.toIntOrNull()?.let {
                            viewModel.setHardModeDuration(it)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(100.dp)
                    .padding(top = 8.dp)
                    .onFocusChanged { focusState ->
                        val wasFocused = isFocused
                        isFocused = focusState.isFocused

                        if (isFocused && !wasFocused) {
                            // Clear on gain focus
                            textFieldValue = TextFieldValue(
                                text = "",
                                selection = TextRange.Zero
                            )
                        } else if (!isFocused && wasFocused) {
                            // Revert to current duration if empty when losing focus
                            if (textFieldValue.text.isEmpty()) {
                                textFieldValue = TextFieldValue(hardModeDuration.toString())
                            }
                        }
                    },
                textStyle = LocalTextStyle.current.copy(fontSize = 24.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray
                )
            )
        }

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
