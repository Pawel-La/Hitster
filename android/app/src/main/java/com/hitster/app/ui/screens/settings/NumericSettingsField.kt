package com.hitster.app.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun NumericSettingsField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value.toString())) }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(value) {
        if (!isFocused && textFieldValue.text != value.toString()) {
            textFieldValue = TextFieldValue(value.toString())
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                if (newValue.text.all { it.isDigit() }) {
                    textFieldValue = newValue
                    newValue.text.toIntOrNull()?.let {
                        onValueChange(it)
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
                        textFieldValue = TextFieldValue(
                            text = "",
                            selection = TextRange.Zero
                        )
                    } else if (!isFocused && wasFocused) {
                        if (textFieldValue.text.isEmpty()) {
                            textFieldValue = TextFieldValue(value.toString())
                        }
                    }
                },
            textStyle = textStyle,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
            )
        )
    }
}
