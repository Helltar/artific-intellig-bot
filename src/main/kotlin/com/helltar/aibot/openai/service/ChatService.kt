package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.chat.ChatRequestData
import com.helltar.aibot.openai.models.chat.ChatResponseData
import com.helltar.aibot.openai.models.common.MessageData
import io.ktor.client.call.*

class ChatService(
    private val model: String,
    private val apiKey: String,
    private val httpClient: HttpClient = KtorHttpClient
) {

    suspend fun getReply(messages: List<MessageData>): String {
        val request = ChatRequestData(model, messages)
        val response: ChatResponseData = httpClient.post(apiKey, Endpoints.CHAT_COMPLETIONS, request).body()
        return response.choices.first().message.content
    }
}
