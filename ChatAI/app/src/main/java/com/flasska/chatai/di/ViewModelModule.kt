package com.flasska.chatai.di

import com.flasska.chatai.presentation.chat.ChatViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { params ->
        ChatViewModel(
            chatId = params.get(),
            repository = get()
        )
    }
}

