package com.flasska.chatai.presentation.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flasska.chatai.presentation.chat.navigateToChat
import com.flasska.chatai.presentation.design_system.ChatColors
import com.flasska.chatai.presentation.navigation.Screen

fun NavGraphBuilder.startScreen(navController: NavController) {
    composable<Screen.Start> {
        StartScreen(navController)
    }
}

@Composable
fun StartScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatColors.ChatBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Создайте чат",
                style = MaterialTheme.typography.headlineMedium,
                color = ChatColors.InputText,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Начните новый диалог с AI-ассистентом",
                style = MaterialTheme.typography.bodyLarge,
                color = ChatColors.InputPlaceholder,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = navController::navigateToChat,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .widthIn(min = 200.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChatColors.SendButtonEnabled
                )
            ) {
                Text(
                    text = "Создать чат",
                    style = MaterialTheme.typography.labelLarge,
                    color = ChatColors.UserMessageText
                )
            }
        }
    }
}

