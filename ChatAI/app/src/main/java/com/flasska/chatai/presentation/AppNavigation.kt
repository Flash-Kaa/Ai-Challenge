package com.flasska.chatai.presentation

import androidx.compose.runtime.*
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
import com.flasska.chatai.presentation.start.startScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val panelViewModel: ChatsPanelViewModel = koinViewModel()
    val panelUiState by panelViewModel.uiState.collectAsStateWithLifecycle()
    
    // Получаем текущий экран для определения chatId
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentChatId = navBackStackEntry?.let { entry ->
        try {
            val screen = entry.toRoute<Screen>()
            when (screen) {
                is Screen.Chat -> screen.id
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Обновляем текущий чат в ViewModel
    LaunchedEffect(currentChatId) {
        panelViewModel.setCurrentChatId(currentChatId)
    }

    ChatsSidePanel(
        chatPreviews = panelUiState.chatPreviews,
        currentChatId = panelUiState.currentChatId ?: currentChatId,
        onChatClick = { chatId ->
            navController.navigateToChat(chatId)
        },
        onNewChatClick = {
            navController.navigateToChat(null)
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
        }
    }
}