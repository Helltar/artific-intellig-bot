package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.ApiUser
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.models.responses.ResponsesRequestData
import com.helltar.aibot.openai.models.responses.ResponsesResponseData
import io.ktor.client.call.*

class ChatService(
    private val model: String,
    private val apiKey: String,
    private val userId: Long,
    private val httpClient: HttpClient = KtorHttpClient
) {

    private companion object {
        const val CACHE_KEY_PREFIX = "chat"
    }

    suspend fun getReply(messages: List<MessageData>, instructions: String): String {
        val request =
            ResponsesRequestData(
                model = model,
                input = messages,
                instructions = instructions,
                promptCacheKey = ApiUser.promptCacheKey(CACHE_KEY_PREFIX, userId),
                safetyIdentifier = ApiUser.safetyIdentifier(userId)
            )

        val response: ResponsesResponseData = httpClient.post(apiKey, Endpoints.RESPONSES, request).body()

        return response.outputText()
    }
}
