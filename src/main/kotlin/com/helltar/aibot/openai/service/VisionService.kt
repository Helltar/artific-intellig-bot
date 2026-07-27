package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.ApiConfig.InputContentType
import com.helltar.aibot.openai.ApiUser
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.common.ContentPartData
import com.helltar.aibot.openai.models.image.VisionMessageData
import com.helltar.aibot.openai.models.image.VisionRequestData
import com.helltar.aibot.openai.models.responses.ResponsesResponseData
import io.ktor.client.call.*
import java.io.File
import java.util.*

class VisionService(
    private val model: String,
    private val apiKey: String,
    private val userId: Long,
    private val httpClient: HttpClient = KtorHttpClient
) {

    private companion object {
        const val CACHE_KEY_PREFIX = "vision"
        val BASE64_Encoder: Base64.Encoder = Base64.getEncoder()
    }

    suspend fun analyzeImage(text: String, image: File, instructions: String, context: String? = null): String {
        val imageBase64 = BASE64_Encoder.encodeToString(image.readBytes())

        val userContent =
            listOf(
                ContentPartData(InputContentType.TEXT, text),
                ContentPartData(InputContentType.IMAGE, imageUrl = "data:image/jpeg;base64,$imageBase64")
            )

        val input =
            buildList {
                context?.let { add(VisionMessageData(ChatRole.SYSTEM, listOf(ContentPartData(InputContentType.TEXT, it)))) }
                add(VisionMessageData(ChatRole.USER, userContent))
            }

        val request =
            VisionRequestData(
                model = model,
                input = input,
                instructions = instructions,
                promptCacheKey = ApiUser.promptCacheKey(CACHE_KEY_PREFIX, userId),
                safetyIdentifier = ApiUser.safetyIdentifier(userId)
            )

        val response: ResponsesResponseData = httpClient.post(apiKey, Endpoints.RESPONSES, request).body()

        return response.outputText()
    }
}
