package com.flasska.chatai.presentation.design_system

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.flasska.chatai.domain.model.ChatPreview
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatsSidePanel(
    chatPreviews: List<ChatPreview>,
    currentChatId: String?,
    onChatClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    isOpen: Boolean,
    onOpenChange: (Boolean) -> Unit,
    screenContent: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = if (isOpen) DrawerValue.Open else DrawerValue.Closed)
    
    // Синхронизация состояния drawer'а с isOpen
    LaunchedEffect(isOpen) {
        if (isOpen && drawerState.currentValue != DrawerValue.Open) {
            drawerState.open()
        } else if (!isOpen && drawerState.currentValue != DrawerValue.Closed) {
            drawerState.close()
        }
    }

    // Обработка закрытия drawer'а через свайп
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Closed && isOpen) {
            onOpenChange(false)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(ChatColors.ChatBackground)
            ) {
                // Заголовок
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Чаты",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = ChatColors.InputText
                    )
                    IconButton(onClick = { onOpenChange(false) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = ChatColors.InputText
                        )
                    }
                }

                HorizontalDivider(color = ChatColors.Divider)

                // Кнопка создания нового чата
                Button(
                    onClick = {
                        onNewChatClick()
                        onOpenChange(false)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ChatColors.SendButtonEnabled
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = ChatColors.UserMessageText
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Новый чат",
                        color = ChatColors.UserMessageText
                    )
                }

                HorizontalDivider(color = ChatColors.Divider)

                // Список чатов
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (chatPreviews.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Нет чатов",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ChatColors.InputPlaceholder
                                )
                            }
                        }
                    } else {
                        items(chatPreviews, key = { it.id }) { chatPreview ->
                            ChatPreviewItem(
                                chatPreview = chatPreview,
                                isSelected = chatPreview.id == currentChatId,
                                onClick = {
                                    onChatClick(chatPreview.id)
                                    onOpenChange(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            screenContent()

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                        )
                    )
                    .clickable { onOpenChange(true) }
                    .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)
                    .align(Alignment.CenterStart)
                    .zIndex(1f)
                    .background(
                        color = ChatColors.SendButtonEnabled,
                        shape = RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                        )
                    )
                    .padding(vertical = 16.dp),

            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Открыть список чатов",
                    tint = ChatColors.UserMessageText
                )
            }
        }
    }
}

@Composable
private fun ChatPreviewItem(
    chatPreview: ChatPreview,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        ChatColors.InputBackground
    } else {
        ChatColors.ChatBackground
    }

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val displayDate = chatPreview.lastMessageTimestamp?.let { dateFormat.format(Date(it)) }
        ?: chatPreview.createdAt.let { dateFormat.format(Date(it)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chatPreview.lastMessage?.take(40)?.let { 
                        if (it.length < chatPreview.lastMessage.length) "$it..." else it
                    } ?: "Новое сообщение",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = ChatColors.InputText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (chatPreview.unreadCount > 0) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = ChatColors.SendButtonEnabled,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = chatPreview.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ChatColors.UserMessageText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayDate,
                style = MaterialTheme.typography.bodySmall,
                color = ChatColors.InputPlaceholder,
                maxLines = 1
            )
        }
    }
}
