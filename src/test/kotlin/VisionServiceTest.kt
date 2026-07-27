import com.helltar.aibot.openai.service.VisionService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import java.io.File
import java.util.*
import kotlin.test.*

class VisionServiceTest {

    private companion object {
        const val USER_ID = 1234567890L
        const val RESPONSE_JSON = """
            {
              "model": "gpt-test",
              "output": [
                { "type": "message", "role": "assistant", "content": [ { "type": "output_text", "text": "A cat." } ] }
              ]
            }
        """
    }

    private fun withTempImage(block: suspend (File, String) -> Unit) {
        val imageBytes = "fake image bytes".toByteArray()
        val image = File.createTempFile("vision-test", ".jpg").apply { writeBytes(imageBytes) }
        try {
            runBlocking { block(image, Base64.getEncoder().encodeToString(imageBytes)) }
        } finally {
            image.delete()
        }
    }

    @Test
    fun `analyzeImage sends base64 data url with input_text and input_image parts`() = withTempImage { image, imageBase64 ->
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)

        val answer = VisionService("gpt-test", "sk-test", USER_ID, httpClient).analyzeImage("what is this?", image, "you are a bot")

        assertEquals("A cat.", answer)
        assertEquals("/v1/responses", httpClient.requestPath)

        val body = Json.parseToJsonElement(httpClient.requestBody).jsonObject
        assertEquals(false, body["store"]?.jsonPrimitive?.boolean)
        assertEquals("you are a bot", body["instructions"]?.jsonPrimitive?.content, "the system prompt goes to instructions")

        val input = body["input"]!!.jsonArray
        assertEquals(1, input.size, "no system message expected when context is null")

        val userMessage = input[0].jsonObject
        assertEquals("user", userMessage["role"]?.jsonPrimitive?.content)

        val content = userMessage["content"]!!.jsonArray
        assertEquals("input_text", content[0].jsonObject["type"]?.jsonPrimitive?.content)
        assertEquals("what is this?", content[0].jsonObject["text"]?.jsonPrimitive?.content)
        assertEquals("input_image", content[1].jsonObject["type"]?.jsonPrimitive?.content)
        assertEquals("data:image/jpeg;base64,$imageBase64", content[1].jsonObject["image_url"]?.jsonPrimitive?.content)
    }

    @Test
    fun `analyzeImage prepends system message when context is given`() = withTempImage { image, _ ->
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)

        VisionService("gpt-test", "sk-test", USER_ID, httpClient).analyzeImage("what is this?", image, "you are a bot", context = "# Context")

        val input = Json.parseToJsonElement(httpClient.requestBody).jsonObject["input"]!!.jsonArray
        assertEquals(2, input.size)

        val systemMessage = input[0].jsonObject
        assertEquals("system", systemMessage["role"]?.jsonPrimitive?.content)

        val systemContent = systemMessage["content"]!!.jsonArray.single().jsonObject
        assertEquals("input_text", systemContent["type"]?.jsonPrimitive?.content)
        assertEquals("# Context", systemContent["text"]?.jsonPrimitive?.content)
        assertNull(systemContent["image_url"])

        assertEquals("user", input[1].jsonObject["role"]?.jsonPrimitive?.content)
    }

    @Test
    fun `analyzeImage identifies the user with an own cache key`() = withTempImage { image, _ ->
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)

        VisionService("gpt-test", "sk-test", USER_ID, httpClient).analyzeImage("what is this?", image, "you are a bot")

        val body = Json.parseToJsonElement(httpClient.requestBody).jsonObject
        val safetyIdentifier = assertNotNull(body["safety_identifier"]?.jsonPrimitive?.content)

        assertFalse(safetyIdentifier.contains(USER_ID.toString()), "the telegram id must not be sent as is")
        assertEquals("vision-$safetyIdentifier", body["prompt_cache_key"]?.jsonPrimitive?.content)
    }
}
