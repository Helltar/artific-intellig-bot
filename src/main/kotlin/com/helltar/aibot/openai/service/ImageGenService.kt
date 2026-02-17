package com.helltar.aibot.openai.service

import com.helltar.aibot.openai.ApiConfig.Endpoints
import com.helltar.aibot.openai.HttpClient
import com.helltar.aibot.openai.models.image.ImageGenRequest
import com.helltar.aibot.openai.models.image.ImageGenResponseData
import java.util.*

class ImageGenService(private val client: HttpClient = HttpClient, private val model: String, private val apiKey: String) {

    private companion object {
        val BASE64_DECODER: Base64.Decoder = Base64.getDecoder()
    }

    suspend fun generateImage(prompt: String, size: Int = 1024): ByteArray {
        val request = ImageGenRequest(model, prompt, n = 1, size = "${size}x${size}")
        val response: ImageGenResponseData = client.post(apiKey, Endpoints.IMAGES_GENERATIONS, request)
        val b64Json = response.data.first().b64Json ?: error("image API returned null b64_json")
        val bytes = BASE64_DECODER.decode(b64Json)
        return bytes
    }
}
