package com.hitster.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val viewModel: SongPlayerViewModel = viewModel()
    NavHost(
        navController = navController,
        startDestination = "settings",
        modifier = modifier
    ) {
        composable("settings") {
            SettingsScreen(
                onPlayClick = { navController.navigate("song_player") },
                viewModel = viewModel
            )
        }
        composable("song_player") {
            SongPlayerScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
