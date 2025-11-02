package com.flasska.chatai.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "chat_ai_prefs"
        private const val KEY_API_KEY = "yandex_api_key"
        private const val KEY_FOLDER_ID = "yandex_folder_id"
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_API_KEY, null)
    }

    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getFolderId(): String? {
        return prefs.getString(KEY_FOLDER_ID, null)
    }

    fun setFolderId(folderId: String) {
        prefs.edit().putString(KEY_FOLDER_ID, folderId).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

