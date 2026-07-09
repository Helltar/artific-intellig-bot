import com.helltar.aibot.openai.service.ImageGenService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import java.util.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ImageGenServiceTest {

    @Test
    fun `generateImage posts prompt and decodes b64_json from response`() = runBlocking {
        val imageBytes = "fake png bytes".toByteArray()
        val b64Json = Base64.getEncoder().encodeToString(imageBytes)
        val httpClient = FakeOpenAiHttpClient("""{ "created": 1, "data": [ { "b64_json": "$b64Json" } ] }""")

        val result = ImageGenService("gpt-image-test", "sk-test", httpClient).generateImage("a cat", size = 512)

        assertContentEquals(imageBytes, result)
        assertEquals("/v1/images/generations", httpClient.requestPath)

        val body = Json.parseToJsonElement(httpClient.requestBody).jsonObject
        assertEquals("gpt-image-test", body["model"]?.jsonPrimitive?.content)
        assertEquals("a cat", body["prompt"]?.jsonPrimitive?.content)
        assertEquals(1, body["n"]?.jsonPrimitive?.int)
        assertEquals("512x512", body["size"]?.jsonPrimitive?.content)
    }
}
