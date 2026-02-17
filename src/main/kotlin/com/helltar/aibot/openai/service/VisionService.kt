package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.ChatContentType
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.image.*
import io.ktor.client.call.*
import java.io.File
import java.util.*

class VisionService(
    private val model: String,
    private val apiKey: String,
    private val httpClient: HttpClient = KtorHttpClient
) {

    suspend fun analyzeImage(text: String, image: File): String {
        val imageBase64 = Base64.getEncoder().encodeToString(image.readBytes())
        val imageData = VisionImageData("data:image/jpeg;base64,$imageBase64")
        val contentTextData = VisionContentData(ChatContentType.TEXT, text)
        val contentImageData = VisionContentData(ChatContentType.IMAGE_URL, imageUrl = imageData)
        val request = VisionRequestData(model, listOf(VisionMessageData(ChatRole.USER, listOf(contentTextData, contentImageData))))
        val response: VisionResponseData = httpClient.post(apiKey, Endpoints.CHAT_COMPLETIONS, request).body()
        return response.choices.first().message.content
    }
}
