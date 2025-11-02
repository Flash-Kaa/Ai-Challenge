package com.flasska.chatai.di

import androidx.room.Room
import com.flasska.chatai.data.api.yandex.YandexApiService
import com.flasska.chatai.data.api.yandex.YandexApiServiceImpl
import com.flasska.chatai.data.local.ChatDatabase
import com.flasska.chatai.data.local.PreferencesManager
import com.flasska.chatai.data.local.dao.ChatDao
import com.flasska.chatai.data.local.dao.MessageDao
import com.flasska.chatai.domain.repository.ChatRepository
import com.flasska.chatai.domain.repository.ChatRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // HTTP Client
    single<HttpClient> {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = false
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    // API Service
    single<YandexApiService> {
        YandexApiServiceImpl(client = get())
    }

    // Room Database
    single<ChatDatabase> {
        Room.databaseBuilder(
            androidContext(),
            ChatDatabase::class.java,
            "chat_database"
        ).build()
    }

    // DAOs
    single<ChatDao> {
        get<ChatDatabase>().chatDao()
    }

    single<MessageDao> {
        get<ChatDatabase>().messageDao()
    }

    // Preferences Manager
    single<PreferencesManager> {
        PreferencesManager(androidContext())
    }

    // Repository
    single<ChatRepository> {
        ChatRepositoryImpl(
            yandexApiService = get(),
            chatDao = get(),
            messageDao = get(),
            preferencesManager = get()
        )
    }
}

