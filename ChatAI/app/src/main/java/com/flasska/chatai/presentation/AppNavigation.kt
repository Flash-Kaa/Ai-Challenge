package com.flasska.chatai.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.flasska.chatai.presentation.chat.chatScreen
import com.flasska.chatai.presentation.chat.navigateToChat
import com.flasska.chatai.presentation.design_system.ChatsPanelViewModel
import com.flasska.chatai.presentation.design_system.ChatsSidePanel
import com.flasska.chatai.presentation.navigation.Screen
import com.flasska.chatai.presentation.settings.navigateToSettings
import com.flasska.chatai.presentation.settings.settingsScreen
import com.flasska.chatai.presentation.start.startScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val panelViewModel: ChatsPanelViewModel = koinViewModel()
    val panelUiState by panelViewModel.uiState.collectAsStateWithLifecycle()

    // Получаем текущий экран для определения chatId
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = navBackStackEntry?.let { entry ->
        try {
            entry.toRoute<Screen>()
        } catch (e: Exception) {
            null
        }
    }

    val currentChatId = currentScreen?.let { screen ->
        when (screen) {
            is Screen.Chat -> screen.id
            else -> null
        }
    }

    // Обновляем текущий чат в ViewModel
    LaunchedEffect(currentChatId) {
        panelViewModel.setCurrentChatId(currentChatId)
    }

    // Определяем, нужно ли показывать боковую панель
    val showSidePanel = currentScreen !is Screen.Settings

    if (showSidePanel) {
        ChatsSidePanel(
            chatPreviews = panelUiState.chatPreviews,
            currentChatId = panelUiState.currentChatId ?: currentChatId,
            onChatClick = { chatId ->
                navController.navigateToChat(chatId)
            },
            onNewChatClick = {
                navController.navigateToChat(null)
            },
            onSettingsClick = {
                navController.navigateToSettings()
            },
            isOpen = panelUiState.isOpen,
            onOpenChange = { isOpen ->
                if (isOpen) {
                    panelViewModel.openPanel()
                } else {
                    panelViewModel.closePanel()
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.startRoute
            ) {
                startScreen(navController)
                chatScreen()
                settingsScreen(navController)
            }
        }
    } else {
        // На экране настроек боковая панель не показывается
        NavHost(
            navController = navController,
            startDestination = Screen.startRoute
        ) {
            startScreen(navController)
            chatScreen()
            settingsScreen(navController)
        }
    }
}