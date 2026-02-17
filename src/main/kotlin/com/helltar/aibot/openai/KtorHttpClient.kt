package com.helltar.aibot.openai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object KtorHttpClient : HttpClient {

    private const val TIMEOUT = 120_000L

    private val client =
        HttpClient(CIO) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                        explicitNulls = false
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT
                connectTimeoutMillis = TIMEOUT
                socketTimeoutMillis = TIMEOUT
            }
        }

    override suspend fun post(apiKey: String, endpoint: String, request: Any): HttpResponse =
        client
            .post(ApiConfig.BASE_URL + endpoint) {
                contentType(ContentType.Application.Json)
                bearerAuth(apiKey)
                setBody(request)
            }
}
