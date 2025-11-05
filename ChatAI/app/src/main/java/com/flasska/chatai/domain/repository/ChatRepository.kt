package com.flasska.chatai.domain.repository

import com.flasska.chatai.data.api.yandex.ApiMessage
import com.flasska.chatai.data.api.yandex.YandexApiService
import com.flasska.chatai.data.local.PreferencesManager
import com.flasska.chatai.data.local.dao.ChatDao
import com.flasska.chatai.data.local.dao.MessageDao
import com.flasska.chatai.data.local.entity.ChatEntity
import com.flasska.chatai.data.local.mapper.toDomain
import com.flasska.chatai.data.local.mapper.toEntity
import com.flasska.chatai.domain.model.Chat
import com.flasska.chatai.domain.model.ChatPreview
import com.flasska.chatai.domain.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.UUID

interface ChatRepository {
    fun getChat(id: String): Flow<Chat>
    suspend fun addMessage(chatId: String, message: Message)
    suspend fun createChat(id: String? = null): Chat
    suspend fun deleteChat(chatId: String)
    suspend fun getAllChats(): Flow<List<Chat>>
    suspend fun getAllChatPreviews(): Flow<List<ChatPreview>>
}

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryImpl(
    private val yandexApiService: YandexApiService,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val preferencesManager: PreferencesManager
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
        val chatEntity = chatDao.getChatById(chatId).first()
        if (chatEntity == null) {
            // Если чата нет, создаем его
            chatDao.insertChat(ChatEntity(id = chatId))
        }

        // Сохраняем сообщение пользователя в БД
        messageDao.insertMessage(message.toEntity(chatId))

        // Получаем все сообщения для отправки в API
        val allMessages = messageDao.getMessagesByChatId(chatId)
            .first()
            .map { it.toDomain() }

        // Добавляем системный промпт
        val systemMessage = ApiMessage(
            role = "system",
            text = RequestUtils.SYSTEM_MESSAGE,
        )

        // Конвертируем сообщения из БД в формат API
        val userApiMessages = allMessages.map { msg ->
            ApiMessage(
                role = if (msg.isFromUser) "user" else "assistant",
                text = msg.text
            )
        }

        // Объединяем системный промпт с историей чата
        val apiMessages = listOf(systemMessage) + userApiMessages

        // Получаем API ключи и модель из настроек
        val apiKey = preferencesManager.getApiKey() ?: ""
        val folderId = preferencesManager.getFolderId() ?: ""
        val model = preferencesManager.getModel()

        // Проверяем, что API ключи настроены
        if (apiKey.isBlank() || folderId.isBlank() ||
            apiKey == "YOUR_API_KEY_HERE" || folderId == "YOUR_FOLDER_ID_HERE"
        ) {
            throw IllegalStateException(
                "API ключи не настроены. Пожалуйста, перейдите в настройки и добавьте YANDEX_API_KEY и YANDEX_FOLDER_ID."
            )
        }

        val result = yandexApiService.sendMessage(
            messages = apiMessages,
            apiKey = apiKey,
            folderId = folderId,
            model = model
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
        val existingChat = chatDao.getChatById(chatId).firstOrNull()

        return if (existingChat != null) {
            // Чат уже существует, возвращаем его
            val messages = messageDao.getMessagesByChatId(chatId).first().map { it.toDomain() }
            existingChat.toDomain(messages)
        } else {
            // Создаем новый чат
            val chat = Chat(id = chatId, messages = emptyList())
            chatDao.insertChat(chat.toEntity())
            chat
        }
    }

    override suspend fun getAllChats(): Flow<List<Chat>> {
        // Для получения всех чатов с сообщениями нужна более сложная логика
        // Пока возвращаем только чаты без сообщений
        // Можно расширить при необходимости
        return chatDao.getAllChats().map { chatEntities ->
            chatEntities.map { chatEntity ->
                chatEntity.toDomain(emptyList())
            }
        }
    }

    override suspend fun deleteChat(chatId: String) {
        // Удаляем все сообщения чата (благодаря CASCADE в БД это произойдет автоматически,
        // но удалим явно для надежности)
        messageDao.deleteMessagesByChatId(chatId)
        // Удаляем сам чат
        chatDao.deleteChatById(chatId)
    }

    override suspend fun getAllChatPreviews(): Flow<List<ChatPreview>> {
        return chatDao.getAllChats()
            .flatMapLatest { chatEntities ->
                flow {
                    val previews = chatEntities.map { chatEntity ->
                        val lastMessage = messageDao.getLastMessageByChatId(chatEntity.id)
                        val firstMessage = messageDao.getFirstMessageByChatId(chatEntity.id)
                        ChatPreview(
                            id = chatEntity.id,
                            firstMessage = firstMessage?.text,
                            lastMessageTimestamp = lastMessage?.timestamp,
                            createdAt = chatEntity.createdAt,
                            unreadCount = 0 // Пока не реализовано
                        )
                    }
                    // Сортируем по дате последнего сообщения или создания, если нет сообщений
                    val sorted = previews.sortedByDescending {
                        it.lastMessageTimestamp ?: it.createdAt
                    }
                    emit(sorted)
                }
            }
            .flowOn(Dispatchers.IO)
    }
}
