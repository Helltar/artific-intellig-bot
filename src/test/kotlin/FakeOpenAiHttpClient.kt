import com.helltar.aibot.openai.KtorHttpClient.openAiDefaults
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/* Uses KtorHttpClient's setup (same Json config, content negotiation, retries, bearer auth)
   but sends requests to a MockEngine, capturing them for assertions.
   Every response after the last one in [statuses] repeats it. */

class FakeOpenAiHttpClient(
    private val responseJson: String,
    private val statuses: List<HttpStatusCode> = listOf(HttpStatusCode.OK)
) : com.helltar.aibot.openai.HttpClient {

    var requestPath = ""
    var requestBody = ""
    var authHeader: String? = null
    var requestCount = 0

    private val client =
        io.ktor.client.HttpClient(
            MockEngine { request ->
                requestPath = request.url.encodedPath
                requestBody = request.body.toByteArray().decodeToString()
                authHeader = request.headers[HttpHeaders.Authorization]
                val status = statuses.getOrElse(requestCount++) { statuses.last() }
                respond(responseJson, status, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        ) {
            openAiDefaults()

            install(HttpRequestRetry) {
                delay { } // no backoff in tests
            }
        }

    override suspend fun post(apiKey: String, endpoint: String, request: Any): HttpResponse =
        client
            .post("https://api.openai.test/v1$endpoint") {
                contentType(ContentType.Application.Json)
                bearerAuth(apiKey)
                setBody(request)
            }
}
