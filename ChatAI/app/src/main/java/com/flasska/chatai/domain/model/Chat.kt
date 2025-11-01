package com.flasska.chatai.domain.model

data class Chat(
    val id: String,
    val messages: List<Message>,
    val createdAt: Long = System.currentTimeMillis()
)

