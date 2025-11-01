package com.flasska.chatai.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.flasska.chatai.domain.model.Message
import com.flasska.chatai.presentation.design_system.ChatColors
import com.flasska.chatai.presentation.navigation.Screen
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID

fun NavController.navigateToChat(id: String? = null) {
    navigate(Screen.Chat(id))
}

fun NavGraphBuilder.chatScreen() {
    composable<Screen.Chat> { backStackEntry ->
        val chat = backStackEntry.toRoute<Screen.Chat>()
        val chatId = chat.id ?: UUID.randomUUID().toString()
        ChatScreen(chatId = chatId)
    }
}

@Composable
fun ChatScreen(
    chatId: String,
    viewModel: ChatViewModel = koinViewModel(parameters = { parametersOf(chatId) })
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Автопрокрутка к новым сообщениям (они в начале списка после reversed)
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            // После reversed последние сообщения (самые новые) будут в начале списка
            // Прокручиваем к началу (индекс 0) чтобы видеть новые сообщения
            kotlinx.coroutines.delay(100) // Небольшая задержка для корректной прокрутки
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatColors.ChatBackground)
    ) {
        // Список сообщений (отображаем в обратном порядке - первое сообщение внизу)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                MessageItem(message = message)
            }
        }

        // Поле ввода и кнопка отправки
        InputBar(
            text = inputText,
            onTextChange = { if (!uiState.isProcessing) inputText = it },
            onSendClick = {
                if (inputText.isNotBlank() && !uiState.isProcessing) {
                    viewModel.handleEvent(ChatEvent.SendMessage(inputText))
                    inputText = ""
                }
            },
            enabled = !uiState.isProcessing
        )
    }
}

@Composable
fun MessageItem(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isFromUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (message.isFromUser) {
                        ChatColors.UserMessageBackground
                    } else {
                        ChatColors.BotMessageBackground
                    }
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isFromUser) {
                    ChatColors.UserMessageText
                } else {
                    ChatColors.BotMessageText
                },
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun InputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = ChatColors.ChatBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp)),
                placeholder = {
                    Text(
                        text = if (enabled) "Введите сообщение..." else "Ожидание ответа...",
                        color = ChatColors.InputPlaceholder
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ChatColors.InputBackground,
                    unfocusedContainerColor = ChatColors.InputBackground,
                    disabledContainerColor = ChatColors.InputBackground,
                    focusedTextColor = if (enabled) ChatColors.InputText else ChatColors.InputPlaceholder,
                    unfocusedTextColor = if (enabled) ChatColors.InputText else ChatColors.InputPlaceholder,
                    disabledTextColor = ChatColors.InputPlaceholder
                ),
                shape = RoundedCornerShape(20.dp),
                singleLine = false,
                maxLines = 4
            )

            IconButton(
                onClick = onSendClick,
                enabled = text.isNotBlank() && enabled,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (text.isNotBlank() && enabled) {
                            ChatColors.SendButtonEnabled
                        } else {
                            ChatColors.SendButtonDisabled
                        }
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = if (text.isNotBlank()) {
                        ChatColors.UserMessageText
                    } else {
                        ChatColors.InputPlaceholder
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}