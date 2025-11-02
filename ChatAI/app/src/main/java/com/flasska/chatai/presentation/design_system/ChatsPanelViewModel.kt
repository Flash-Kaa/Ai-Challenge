package com.flasska.chatai.presentation.design_system

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flasska.chatai.domain.model.ChatPreview
import com.flasska.chatai.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ChatsPanelUiState(
    val chatPreviews: List<ChatPreview> = emptyList(),
    val currentChatId: String? = null,
    val isLoading: Boolean = true,
    val isOpen: Boolean = false,
    val error: String? = null
)

class ChatsPanelViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatsPanelUiState())
    val uiState: StateFlow<ChatsPanelUiState> = _uiState.asStateFlow()

    init {
        // Загружаем список чатов с превью
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.getAllChatPreviews()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        chatPreviews = emptyList(),
                        error = error.message ?: "Ошибка загрузки чатов"
                    )
                }
                .collect { chatPreviews ->
                    _uiState.value = _uiState.value.copy(
                        chatPreviews = chatPreviews,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    /**
     * Устанавливает текущий открытый чат
     */
    fun setCurrentChatId(chatId: String?) {
        _uiState.value = _uiState.value.copy(
            currentChatId = chatId
        )
    }

    /**
     * Создает новый чат в базе данных
     * @param chatId Опциональный ID чата. Если не указан, будет создан новый UUID
     * @return ID созданного чата
     */
    suspend fun createNewChat(chatId: String? = null): String {
        return try {
            val chat = repository.createChat(chatId)
            chat.id
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = e.message ?: "Ошибка создания чата"
            )
            throw e
        }
    }

    /**
     * Удаляет чат из базы данных вместе со всеми его сообщениями
     * @param chatId ID чата для удаления
     */
    fun deleteChat(chatId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                repository.deleteChat(chatId)

                // Если удаляемый чат был текущим, сбрасываем currentChatId
                if (_uiState.value.currentChatId == chatId) {
                    _uiState.value = _uiState.value.copy(
                        currentChatId = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Ошибка удаления чата"
                )
            }
        }
    }

    /**
     * Обновляет список чатов вручную
     * Обновление также происходит автоматически через Flow при изменении данных в БД
     */
    fun refreshChats() {
        loadChats()
    }

    /**
     * Очищает ошибку из состояния
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Переключает состояние панели (открыта/закрыта)
     */
    fun togglePanel() {
        _uiState.value = _uiState.value.copy(
            isOpen = !_uiState.value.isOpen
        )
    }

    /**
     * Открывает панель чатов
     */
    fun openPanel() {
        _uiState.value = _uiState.value.copy(
            isOpen = true
        )
    }

    /**
     * Закрывает панель чатов
     */
    fun closePanel() {
        _uiState.value = _uiState.value.copy(
            isOpen = false
        )
    }
}

