package com.flasska.chatai.domain.repository

import com.flasska.chatai.data.api.ApiConfig
import com.flasska.chatai.data.api.yandex.ApiMessage
import com.flasska.chatai.data.api.yandex.YandexApiService
import com.flasska.chatai.data.local.dao.ChatDao
import com.flasska.chatai.data.local.dao.MessageDao
import com.flasska.chatai.data.local.entity.ChatEntity
import com.flasska.chatai.data.local.mapper.toDomain
import com.flasska.chatai.data.local.mapper.toEntity
import com.flasska.chatai.domain.model.Chat
import com.flasska.chatai.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

interface ChatRepository {
    fun getChat(id: String): Flow<Chat>
    suspend fun addMessage(chatId: String, message: Message)
    suspend fun createChat(id: String? = null): Chat
    fun getAllChats(): Flow<List<Chat>>
}

class ChatRepositoryImpl(
    private val yandexApiService: YandexApiService,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) : ChatRepository {

    override fun getChat(id: String): Flow<Chat> {
        return combine(
            chatDao.getChatById(id),
            messageDao.getMessagesByChatId(id)
        ) { chatEntity, messageEntities ->
            val messages = messageEntities.map { it.toDomain() }
            chatEntity?.toDomain(messages) ?: Chat(id = id, messages = messages)
        }
    }

    override suspend fun addMessage(chatId: String, message: Message) {
        // Убеждаемся, что чат существует в БД
        val chatEntity = chatDao.getChatById(chatId).let { flow ->
            flow.first()
        }
        if (chatEntity == null) {
            // Если чата нет, создаем его
            chatDao.insertChat(ChatEntity(id = chatId))
        }
        
        // Сохраняем сообщение пользователя в БД
        messageDao.insertMessage(message.toEntity(chatId))

        // Получаем все сообщения для отправки в API
        val allMessages = messageDao.getMessagesByChatId(chatId).let { flow ->
            flow.first().map { it.toDomain() }
        }

        // Отправляем запрос к Yandex GPT API
        val apiMessages = allMessages.map { msg ->
            ApiMessage(
                role = if (msg.isFromUser) "user" else "assistant",
                text = msg.text
            )
        }

        // Проверяем, что API ключи настроены
        if (ApiConfig.YANDEX_API_KEY == "YOUR_API_KEY_HERE" || 
            ApiConfig.YANDEX_FOLDER_ID == "YOUR_FOLDER_ID_HERE") {
            throw IllegalStateException(
                "API ключи не настроены. Пожалуйста, добавьте YANDEX_API_KEY и YANDEX_FOLDER_ID в ApiConfig.kt. " +
                "См. документацию в файле ApiConfig.kt для получения инструкций."
            )
        }

        val result = yandexApiService.sendMessage(
            messages = apiMessages,
            apiKey = ApiConfig.YANDEX_API_KEY,
            folderId = ApiConfig.YANDEX_FOLDER_ID
        )

        result.fold(
            onSuccess = { assistantMessage ->
                // Сохраняем ответ от AI в БД
                val botMessage = Message(
                    text = assistantMessage.text,
                    isFromUser = false
                )
                messageDao.insertMessage(botMessage.toEntity(chatId))
            },
            onFailure = { error ->
                // В случае ошибки пробрасываем исключение
                throw error
            }
        )
    }

    override suspend fun createChat(id: String?): Chat {
        val chatId = id ?: UUID.randomUUID().toString()
        // Проверяем, существует ли уже чат
        val existingChat = chatDao.getChatById(chatId).let { flow ->
            flow.first()
        }
        
        return if (existingChat != null) {
            // Чат уже существует, возвращаем его
            val messages = messageDao.getMessagesByChatId(chatId).let { flow ->
                flow.first().map { it.toDomain() }
            }
            existingChat.toDomain(messages)
        } else {
            // Создаем новый чат
            val chat = Chat(id = chatId, messages = emptyList())
            chatDao.insertChat(chat.toEntity())
            chat
        }
    }

    override fun getAllChats(): Flow<List<Chat>> {
        // Для получения всех чатов с сообщениями нужна более сложная логика
        // Пока возвращаем только чаты без сообщений
        // Можно расширить при необходимости
        return chatDao.getAllChats().map { chatEntities ->
            chatEntities.map { chatEntity ->
                chatEntity.toDomain(emptyList())
            }
        }
    }
}

