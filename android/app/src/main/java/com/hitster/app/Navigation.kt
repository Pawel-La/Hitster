package com.hitster.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "before_play",
        modifier = modifier
    ) {
        composable("before_play") {
            BeforePlayScreen(onPlayClick = { navController.navigate("song_player") })
        }
        composable("song_player") {
            SongPlayerScreen()
        }
    }
}
