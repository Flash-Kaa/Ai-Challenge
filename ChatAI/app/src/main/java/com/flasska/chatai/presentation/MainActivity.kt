package com.flasska.chatai.presentation

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.flasska.chatai.di.appModule
import com.flasska.chatai.di.viewModelModule
import com.flasska.chatai.ui.theme.ChatAITheme
import com.yandex.div.R
import com.yandex.div.core.Div2Context
import com.yandex.div.core.DivConfiguration
import com.yandex.div.core.DivKit
import com.yandex.div.core.DivKitConfiguration
import com.yandex.div.picasso.PicassoDivImageLoader
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ChatAIApplication)
            modules(appModule, viewModelModule)
        }

        DivKit.configure(DivKitConfiguration.Builder().build())
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChatAITheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }


        }
    }
}
