package com.helltar.aibot.openai

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.network.sockets.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

object KtorHttpClient : HttpClient {

    private const val TIMEOUT = 120_000L
    private const val MAX_RETRIES = 2

    // the statuses the official sdks retry: timeout, conflict, rate limit and server errors
    private val RETRYABLE_STATUSES = setOf(408, 409, 429)

    val json =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

    private val client =
        HttpClient(CIO) {
            openAiDefaults()

            install(HttpTimeout) {
                requestTimeoutMillis = TIMEOUT
                connectTimeoutMillis = TIMEOUT
                socketTimeoutMillis = TIMEOUT
            }
        }

    // shared with the tests, so they check the real configuration
    fun HttpClientConfig<*>.openAiDefaults() {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpRequestRetry) {
            retryIf(MAX_RETRIES) { _, response ->
                response.status.value in RETRYABLE_STATUSES || response.status.value >= 500
            }

            // a network failure is retried, a timed out generation is not: it would only double the wait and the cost
            retryOnExceptionIf(MAX_RETRIES) { _, cause ->
                cause is IOException && cause !is HttpRequestTimeoutException && cause !is SocketTimeoutException
            }

            exponentialDelay()
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
