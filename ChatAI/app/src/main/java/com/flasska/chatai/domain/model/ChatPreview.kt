package com.flasska.chatai.domain.model

data class ChatPreview(
    val id: String,
    val lastMessage: String?,
    val lastMessageTimestamp: Long?,
    val createdAt: Long,
    val unreadCount: Int = 0
)

