package com.flasska.chatai.data.local.mapper

import com.flasska.chatai.data.local.entity.ChatEntity
import com.flasska.chatai.data.local.entity.MessageEntity
import com.flasska.chatai.domain.model.Chat
import com.flasska.chatai.domain.model.Message

fun ChatEntity.toDomain(messages: List<Message>): Chat {
    return Chat(
        id = id,
        messages = messages,
        createdAt = createdAt
    )
}

fun Chat.toEntity(): ChatEntity {
    return ChatEntity(
        id = id,
        createdAt = createdAt
    )
}

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        text = text,
        isFromUser = isFromUser,
        timestamp = timestamp
    )
}

fun Message.toEntity(chatId: String): MessageEntity {
    return MessageEntity(
        id = id,
        chatId = chatId,
        text = text,
        isFromUser = isFromUser,
        timestamp = timestamp
    )
}

