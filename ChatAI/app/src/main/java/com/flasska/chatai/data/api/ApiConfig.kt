package com.flasska.chatai.data.api

/**
 * Конфигурация для YandexGPT API
 * 
 * Как получить параметры:
 * 
 * 1. API_KEY (API ключ):
 *    - Зайдите на https://cloud.yandex.ru/
 *    - Войдите или зарегистрируйтесь
 *    - Перейдите в раздел "IAM" → "API-ключи"
 *    - Нажмите "Создать API-ключ"
 *    - Скопируйте созданный ключ и вставьте в YANDEX_API_KEY
 * 
 * 2. FOLDER_ID (ID папки):
 *    - В Yandex Cloud Console перейдите в раздел "Все ресурсы" или "Каталоги"
 *    - Выберите каталог (папку), или создайте новый
 *    - В свойствах каталога найдите "ID каталога" (например: b1g...)
 *    - Скопируйте ID и вставьте в YANDEX_FOLDER_ID
 * 
 * Документация: https://cloud.yandex.ru/docs/yandexgpt/
 * 
 * ВАЖНО: После получения ключей необходимо:
 * 1. Убедиться, что у сервисного аккаунта есть роль 'ai.languageModels.user'
 * 2. Активировать сервис YandexGPT в консоли Yandex Cloud:
 *    - Перейдите в раздел "Сервисы" → "YandexGPT"
 *    - Активируйте сервис в вашем каталоге
 * 
 * Для продакшена рекомендуется:
 * - Использовать BuildConfig или local.properties
 * - Или хранить в SecurePreferences/EncryptedSharedPreferences
 * - Никогда не коммитить ключи в Git!
 */
object ApiConfig {
    const val YANDEX_API_KEY = ""
    const val YANDEX_FOLDER_ID = ""
}

