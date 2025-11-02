package com.flasska.chatai.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Start : Screen

    @Serializable
    data class Chat(
        val id: String? = null,
    ) : Screen {
        fun createRoute(id: String?) = if (id == null) "chat" else "chat/$id"
    }

    @Serializable
    data object Settings : Screen

    companion object {
        val startRoute = Start
    }
}

