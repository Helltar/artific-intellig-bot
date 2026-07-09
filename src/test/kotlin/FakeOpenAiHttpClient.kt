import com.helltar.aibot.openai.KtorHttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

/* Replicates KtorHttpClient's setup (same Json config, content negotiation, bearer auth)
   but sends requests to a MockEngine, capturing them for assertions. */

class FakeOpenAiHttpClient(private val responseJson: String) : com.helltar.aibot.openai.HttpClient {

    var requestPath = ""
    var requestBody = ""
    var authHeader: String? = null

    private val client =
        io.ktor.client.HttpClient(
            MockEngine { request ->
                requestPath = request.url.encodedPath
                requestBody = request.body.toByteArray().decodeToString()
                authHeader = request.headers[HttpHeaders.Authorization]
                respond(responseJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }
        ) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(KtorHttpClient.json)
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
