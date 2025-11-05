package com.flasska.chatai.presentation.chat

import androidx.activity.compose.LocalActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.flasska.chatai.domain.model.Message
import com.flasska.chatai.presentation.design_system.ChatColors
import com.flasska.chatai.presentation.navigation.Screen
import com.yandex.div.DivDataTag
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.view2.Div2View
import com.yandex.div.data.DivParsingEnvironment
import com.yandex.div.json.ParsingErrorLogger
import com.yandex.div.picasso.PicassoDivImageLoader
import com.yandex.div2.DivData
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.util.UUID
import com.yandex.div.R as DivR

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
        if (message.isFromUser) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(ChatColors.UserMessageBackground)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.text,
                    color = ChatColors.UserMessageText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            DivKitView(message.text.drop(3).dropLast(3))
        }
    }
}

@Composable
fun DivKitView(divJson: String) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val divContext = remember(context, lifecycleOwner, activity) {
        activity?.let {
            Div2Context(
                baseContext = activity,
                configuration = DivConfiguration.Builder(PicassoDivImageLoader(context)).build(),
            )
        }
    }

    divContext?.let {
        AndroidView(
            factory = { Div2View(divContext) },
            modifier = Modifier.widthIn(max = 280.dp),
            update = {
                try {
                    val divData = JSONObject(divJson).let {
                        val templates = it.optJSONObject("templates")
                        val card = it.getJSONObject("card")
                        val environment = DivParsingEnvironment(ParsingErrorLogger.LOG)
                        if (templates != null) {
                            environment.parseTemplates(templates)
                        }
                        DivData(environment, card)
                    }
                    it.setData(divData, DivDataTag("div2"))
                } catch (e: Exception) {

                    // Fallback to a simple text view if DivKit parsing fails
                    // You can also log the error here
                }
            }
        )
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
