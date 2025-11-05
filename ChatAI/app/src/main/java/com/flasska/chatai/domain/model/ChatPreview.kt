package com.flasska.chatai.domain.model

data class ChatPreview(
    val id: String,
    val firstMessage: String?,
    val lastMessageTimestamp: Long?,
    val createdAt: Long,
    val unreadCount: Int = 0
)


