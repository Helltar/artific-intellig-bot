package com.helltar.aibot.openai

import io.ktor.client.statement.*

interface HttpClient {

    suspend fun post(apiKey: String, endpoint: String, request: Any): HttpResponse
}
