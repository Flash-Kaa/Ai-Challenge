package com.flasska.chatai.data.api.yandex

import kotlinx.serialization.Serializable

@Serializable
data class YandexApiRequest(
    val modelUri: String,
    val completionOptions: CompletionOptions,
    val messages: List<ApiMessage>
)

@Serializable
data class CompletionOptions(
    val stream: Boolean = false,
    val temperature: Double = 0.6,
    val maxTokens: String = "2000"
)

@Serializable
data class ApiMessage(
    val role: String, // "user" or "assistant"
    val text: String
)

@Serializable
data class YandexApiResponse(
    val result: ResponseResult
)

@Serializable
data class ResponseResult(
    val alternatives: List<Alternative>,
    val usage: Usage? = null,
    val modelVersion: String? = null
)

@Serializable
data class Alternative(
    val message: ApiMessage,
    val status: String? = null
)

@Serializable
data class Usage(
    val inputTextTokens: String? = null,
    val completionTokens: String? = null,
    val totalTokens: String? = null
)

@Serializable
data class YandexApiError(
    val error: ErrorDetails? = null,
    val message: String? = null,
    val code: Int? = null
)

@Serializable
data class ErrorDetails(
    val message: String? = null,
    val code: Int? = null
)

