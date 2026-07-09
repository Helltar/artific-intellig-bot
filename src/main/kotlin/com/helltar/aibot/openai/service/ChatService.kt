package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.models.responses.ResponsesRequestData
import com.helltar.aibot.openai.models.responses.ResponsesResponseData
import io.ktor.client.call.*

class ChatService(
    private val model: String,
    private val apiKey: String,
    private val httpClient: HttpClient = KtorHttpClient
) {

    suspend fun getReply(messages: List<MessageData>): String {
        val request = ResponsesRequestData(model, messages)
        val response: ResponsesResponseData = httpClient.post(apiKey, Endpoints.RESPONSES, request).body()
        return response.outputText()
    }
}
