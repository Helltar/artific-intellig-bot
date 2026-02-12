package com.helltar.aibot.openai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

object HttpClient {

    private const val DEFAULT_TIMEOUT = 120_000L

    private val mutex = Mutex()
    private var ktorClient: io.ktor.client.HttpClient? = null

    suspend fun client(): io.ktor.client.HttpClient =
        ktorClient ?: mutex.withLock { ktorClient ?: createHttpClient().also { ktorClient = it } }

    suspend inline fun <reified T> post(apiKey: String, endpoint: String, request: Any): T =
        client()
            .post(ApiConfig.BASE_URL + endpoint) {
                contentType(ContentType.Application.Json)
                bearerAuth(apiKey)
                setBody(request)
            }
            .body()

    private fun createHttpClient(): io.ktor.client.HttpClient {
        return HttpClient(CIO) {
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
                requestTimeoutMillis = DEFAULT_TIMEOUT
                connectTimeoutMillis = DEFAULT_TIMEOUT
                socketTimeoutMillis = DEFAULT_TIMEOUT
            }
        }
    }
}
