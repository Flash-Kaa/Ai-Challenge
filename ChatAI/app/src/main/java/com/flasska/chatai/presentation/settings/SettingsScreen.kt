package com.flasska.chatai.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.flasska.chatai.data.api.yandex.YandexApiService
import com.flasska.chatai.data.local.PreferencesManager
import com.flasska.chatai.presentation.design_system.ChatColors
import com.flasska.chatai.presentation.navigation.Screen
import org.koin.androidx.compose.get

fun NavController.navigateToSettings() {
    navigate(Screen.Settings)
}

fun NavGraphBuilder.settingsScreen(navController: NavController) {
    composable<Screen.Settings> {
        SettingsScreen(navController = navController)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    preferencesManager: PreferencesManager = get(),
    yandexApiService: YandexApiService = get()
) {
    // Загружаем текущие значения
    var apiKey by remember {
        mutableStateOf(preferencesManager.getApiKey() ?: "")
    }
    var folderId by remember {
        mutableStateOf(preferencesManager.getFolderId() ?: "")
    }
    var selectedModel by remember {
        mutableStateOf(preferencesManager.getModel() ?: "yandexgpt")
    }
    var modelExpanded by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }

    // Состояние загрузки моделей
    var availableModels by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoadingModels by remember { mutableStateOf(false) }
    var modelsError by remember { mutableStateOf<String?>(null) }

    // Функция для получения читаемого названия модели
    fun getModelDisplayName(modelId: String): String {
        return when {
            modelId.contains("yandexgpt-lite", ignoreCase = true) -> "YandexGPT Lite (быстрая)"
            modelId.contains(
                "yandexgpt-pro",
                ignoreCase = true
            ) -> "YandexGPT Pro (профессиональная)"

            modelId.contains("yandexgpt", ignoreCase = true) -> "YandexGPT (базовая)"
            else -> modelId
        }
    }

    // Загружаем модели при изменении API ключа или folder ID
    LaunchedEffect(apiKey, folderId) {
        if (apiKey.isNotBlank() && folderId.isNotBlank() &&
            apiKey != "YOUR_API_KEY_HERE" && folderId != "YOUR_FOLDER_ID_HERE"
        ) {
            isLoadingModels = true
            modelsError = null
            yandexApiService.getModels(apiKey, folderId).fold(
                onSuccess = { models ->
                    availableModels = models.map { modelInfo ->
                        modelInfo.id to getModelDisplayName(modelInfo.id)
                    }
                    isLoadingModels = false
                },
                onFailure = { error ->
                    modelsError = error.message
                    isLoadingModels = false
                    // В случае ошибки используем дефолтные модели
                    availableModels = listOf(
                        "yandexgpt" to "YandexGPT (базовая)",
                        "yandexgpt-lite" to "YandexGPT Lite (быстрая)",
                        "yandexgpt-pro" to "YandexGPT Pro (профессиональная)"
                    )
                }
            )
        } else {
            // Если ключи не заданы, используем дефолтные модели
            availableModels = listOf(
                "yandexgpt" to "YandexGPT (базовая)",
                "yandexgpt-lite" to "YandexGPT Lite (быстрая)",
                "yandexgpt-pro" to "YandexGPT Pro (профессиональная)"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatColors.ChatBackground)
    ) {
        // Заголовок с кнопкой назад
        TopAppBar(
            title = {
                Text(
                    text = "Настройки",
                    color = ChatColors.InputText,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Назад",
                        tint = ChatColors.InputText
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatColors.ChatBackground
            )
        )

        HorizontalDivider(color = ChatColors.Divider)

        // Содержимое экрана
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Информационный блок
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ChatColors.InputBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Как получить параметры:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ChatColors.InputText
                    )
                    Text(
                        text = "1. API_KEY: Зайдите на cloud.yandex.ru → IAM → API-ключи → Создать API-ключ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ChatColors.InputText
                    )
                    Text(
                        text = "2. FOLDER_ID: В консоли Yandex Cloud найдите ID каталога в свойствах каталога",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ChatColors.InputText
                    )
                }
            }

            // Поле для API ключа
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "API ключ (YANDEX_API_KEY)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = ChatColors.InputText
                )
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Введите API ключ",
                            color = ChatColors.InputPlaceholder
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ChatColors.InputBackground,
                        unfocusedContainerColor = ChatColors.InputBackground,
                        focusedTextColor = ChatColors.InputText,
                        unfocusedTextColor = ChatColors.InputText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Поле для Folder ID
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ID каталога (YANDEX_FOLDER_ID)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = ChatColors.InputText
                )
                OutlinedTextField(
                    value = folderId,
                    onValueChange = { folderId = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Введите ID каталога",
                            color = ChatColors.InputPlaceholder
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ChatColors.InputBackground,
                        unfocusedContainerColor = ChatColors.InputBackground,
                        focusedTextColor = ChatColors.InputText,
                        unfocusedTextColor = ChatColors.InputText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Выбор модели
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Модель",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = ChatColors.InputText
                )
                ExposedDropdownMenuBox(
                    expanded = modelExpanded && !isLoadingModels && availableModels.isNotEmpty(),
                    onExpandedChange = {
                        if (!isLoadingModels && availableModels.isNotEmpty()) modelExpanded = it
                    }
                ) {
                    OutlinedTextField(
                        value = when {
                            isLoadingModels -> "Загрузка моделей..."
                            availableModels.isEmpty() -> "Модели не загружены"
                            else -> availableModels.firstOrNull { it.first == selectedModel }?.second
                                ?: selectedModel
                        },
                        onValueChange = { },
                        readOnly = true,
                        enabled = !isLoadingModels && availableModels.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        trailingIcon = {
                            if (isLoadingModels) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = ChatColors.InputPlaceholder
                                )
                            } else {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelExpanded)
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Выберите модель",
                                color = ChatColors.InputPlaceholder
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ChatColors.InputBackground,
                            unfocusedContainerColor = ChatColors.InputBackground,
                            disabledContainerColor = ChatColors.InputBackground,
                            focusedTextColor = ChatColors.InputText,
                            unfocusedTextColor = ChatColors.InputText,
                            disabledTextColor = ChatColors.InputPlaceholder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = modelExpanded && !isLoadingModels,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        availableModels.forEach { (modelKey, modelName) ->
                            DropdownMenuItem(
                                text = { Text(text = modelName) },
                                onClick = {
                                    selectedModel = modelKey
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }
                // Сообщение об ошибке загрузки моделей (если есть, но не критично)
                if (modelsError != null && availableModels.isNotEmpty()) {
                    Text(
                        text = "Используются модели по умолчанию. Ошибка: $modelsError",
                        style = MaterialTheme.typography.bodySmall,
                        color = ChatColors.InputPlaceholder,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Кнопка сохранения
            Button(
                onClick = {
                    if (apiKey.isNotBlank() && folderId.isNotBlank()) {
                        preferencesManager.setApiKey(apiKey)
                        preferencesManager.setFolderId(folderId)
                        preferencesManager.setModel(selectedModel)
                        showSuccessMessage = true
                        showErrorMessage = null
                    } else {
                        showErrorMessage = "Пожалуйста, заполните все поля"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChatColors.SendButtonEnabled
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Сохранить",
                    color = ChatColors.UserMessageText,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Сообщение об успехе
            if (showSuccessMessage) {
                LaunchedEffect(showSuccessMessage) {
                    kotlinx.coroutines.delay(3000)
                    showSuccessMessage = false
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = ChatColors.SendButtonEnabled.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Настройки успешно сохранены!",
                        modifier = Modifier.padding(16.dp),
                        color = ChatColors.InputText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Сообщение об ошибке
            showErrorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
