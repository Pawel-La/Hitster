package com.hitster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hitster.app.data.SongResponse
import com.hitster.app.network.NetworkModule
import com.hitster.app.ui.theme.AppTheme
import kotlinx.coroutines.launch

class ApiTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiTestScreen()
                }
            }
        }
    }
}

@Composable
fun ApiTestScreen() {
    var songs by remember { mutableStateOf<List<SongResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            songs = NetworkModule.apiService.getSongs()
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Unknown error"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .statusBarsPadding()
            ) {
                Text(if (isLoading) "Loading..." else "Fetch Songs")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage != null) {
                Text(
                    text = "Error: $errorMessage",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (songs.isEmpty() && !isLoading && errorMessage == null) {
                Text(
                    text = "No songs fetched yet.",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(songs) { song ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = song.artist,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Year: ${song.year}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
