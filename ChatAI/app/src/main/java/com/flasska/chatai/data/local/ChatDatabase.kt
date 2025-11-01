package com.flasska.chatai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flasska.chatai.data.local.dao.ChatDao
import com.flasska.chatai.data.local.dao.MessageDao
import com.flasska.chatai.data.local.entity.ChatEntity
import com.flasska.chatai.data.local.entity.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}

