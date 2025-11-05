package com.flasska.chatai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val createdAt: Long = System.currentTimeMillis()
)
