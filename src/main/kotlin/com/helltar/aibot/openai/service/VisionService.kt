package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.ChatContentPartType
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.chat.ChatResponseData
import com.helltar.aibot.openai.models.common.ContentPartData
import com.helltar.aibot.openai.models.common.ImageUrlData
import com.helltar.aibot.openai.models.image.VisionMessageData
import com.helltar.aibot.openai.models.image.VisionRequestData
import io.ktor.client.call.*
import java.io.File
import java.util.*

class VisionService(
    private val model: String,
    private val apiKey: String,
    private val httpClient: HttpClient = KtorHttpClient
) {

    private companion object {
        val BASE64_Encoder: Base64.Encoder = Base64.getEncoder()
    }

    suspend fun analyzeImage(text: String, image: File, systemPrompt: String? = null): String {
        val imageBase64 = BASE64_Encoder.encodeToString(image.readBytes())

        val userContent =
            listOf(
                ContentPartData(ChatContentPartType.TEXT, text),
                ContentPartData(ChatContentPartType.IMAGE_URL, imageUrl = ImageUrlData("data:image/jpeg;base64,$imageBase64"))
            )

        val messages =
            buildList {
                systemPrompt?.let { add(VisionMessageData(ChatRole.SYSTEM, listOf(ContentPartData(ChatContentPartType.TEXT, it)))) }
                add(VisionMessageData(ChatRole.USER, userContent))
            }

        val request = VisionRequestData(model, messages)
        val response: ChatResponseData = httpClient.post(apiKey, Endpoints.CHAT_COMPLETIONS, request).body()

        return response.choices.first().message.content
    }
}
