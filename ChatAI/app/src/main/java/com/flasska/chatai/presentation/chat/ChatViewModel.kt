package com.flasska.chatai.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flasska.chatai.domain.model.Message
import com.flasska.chatai.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val chatId: String,
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ChatEvent>(extraBufferCapacity = 10)
    
    init {
        // Подписываемся на изменения чата из БД
        viewModelScope.launch(Dispatchers.Default) {
            repository.getChat(chatId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
                .collect { chat ->
                    val lastMessage = chat.messages.lastOrNull()
                    val isProcessing = lastMessage?.isFromUser == true && 
                        chat.messages.none { 
                            !it.isFromUser && it.timestamp > (lastMessage?.timestamp ?: 0) 
                        }
                    
                    _uiState.value = _uiState.value.copy(
                        messages = chat.messages,
                        isLoading = false,
                        error = null,
                        isProcessing = isProcessing
                    )
                }
        }
        
        // Создаем чат в БД, если его нет
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.createChat(chatId)
            } catch (e: Exception) {
                // Игнорируем ошибку, если чат уже существует
            }
        }

        // Обрабатываем события
        viewModelScope.launch(Dispatchers.Default) {
            _events.collect { event ->
                when (event) {
                    is ChatEvent.SendMessage -> {
                        handleSendMessage(event.text)
                    }
                }
            }
        }
    }

    fun handleEvent(event: ChatEvent) {
        viewModelScope.launch(Dispatchers.Default) {
            _events.emit(event)
        }
    }

    private suspend fun handleSendMessage(text: String) {
        if (text.isBlank()) return
        if (_uiState.value.isProcessing) return // Блокируем отправку если уже обрабатывается

        // Устанавливаем флаг обработки
        _uiState.value = _uiState.value.copy(isProcessing = true)

        val message = Message(
            text = text.trim(),
            isFromUser = true
        )

        try {
            repository.addMessage(chatId, message)
        } catch (e: Exception) {
            // При ошибке сбрасываем флаг обработки
            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                error = e.message
            )
        }
    }
}

