package com.flasska.chatai.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.flasska.chatai.presentation.chat.chatScreen
import com.flasska.chatai.presentation.navigation.Screen
import com.flasska.chatai.presentation.start.startScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.startRoute
    ) {
        startScreen(navController)
        chatScreen()
    }
}