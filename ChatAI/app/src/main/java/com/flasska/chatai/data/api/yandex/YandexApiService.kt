package com.flasska.chatai.data.api.yandex

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface YandexApiService {
    suspend fun sendMessage(
        messages: List<ApiMessage>,
        apiKey: String,
        folderId: String
    ): Result<ApiMessage>
}

class YandexApiServiceImpl(
    private val client: HttpClient,
    private val baseUrl: String = "https://llm.api.cloud.yandex.net"
) : YandexApiService {

    override suspend fun sendMessage(
        messages: List<ApiMessage>,
        apiKey: String,
        folderId: String
    ): Result<ApiMessage> {
        return try {
            val request = YandexApiRequest(
                modelUri = "gpt://$folderId/yandexgpt/latest",
                completionOptions = CompletionOptions(),
                messages = messages
            )

            val httpResponse = client.post("$baseUrl/foundationModels/v1/completion") {
                header("Authorization", "Bearer $apiKey")
                header("x-folder-id", folderId)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            // Проверяем статус ответа
            when (httpResponse.status.value) {
                in 200..299 -> {
                    // Успешный ответ
                    try {
                        val response: YandexApiResponse = httpResponse.body()
                        val assistantMessage = response.result.alternatives.firstOrNull()?.message
                            ?: return Result.failure(Exception("Пустой ответ от API"))
                        Result.success(assistantMessage)
                    } catch (e: Exception) {
                        // Пытаемся прочитать тело ответа для диагностики
                        val responseBody = httpResponse.body<String>()
                        Result.failure(Exception("Ошибка десериализации ответа: ${e.message}. Тело ответа: $responseBody"))
                    }
                }

                else -> {
                    // Ошибка API
                    try {
                        val errorBody: YandexApiError = httpResponse.body()
                        val errorMessage = errorBody.error?.message
                            ?: errorBody.message
                            ?: "Неизвестная ошибка API (${httpResponse.status.value})"
                        Result.failure(Exception(errorMessage))
                    } catch (e: Exception) {
                        // Если не удалось десериализовать ошибку, читаем как строку
                        val errorBody = httpResponse.body<String>()
                        Result.failure(Exception("Ошибка API (${httpResponse.status.value}): $errorBody"))
                    }
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(
                Exception(
                    "Не удалось найти адрес '$baseUrl'. " +
                            "Возможные причины:\n" +
                            "1. Сервис YandexGPT не активирован в вашем каталоге\n" +
                            "2. Проверьте настройки интернета\n" +
                            "3. Убедитесь, что используете правильный endpoint"
                )
            )
        } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
            Result.failure(Exception("Не удалось подключиться к серверу. Проверьте интернет-соединение."))
        } catch (e: io.ktor.client.network.sockets.SocketTimeoutException) {
            Result.failure(Exception("Превышено время ожидания ответа от сервера."))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка при отправке запроса: ${e.message}"))
        }
    }
}

