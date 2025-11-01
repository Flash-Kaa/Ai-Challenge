package com.flasska.chatai.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Start : Screen

    @Serializable
    data class Chat(
        val id: String? = null,
    ) : Screen

    companion object {
        val startRoute = Start
    }
}

