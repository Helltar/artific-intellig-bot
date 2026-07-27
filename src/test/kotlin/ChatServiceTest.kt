import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.service.ChatService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.*

class ChatServiceTest {

    private companion object {
        const val USER_ID = 1234567890L
        const val RESPONSE_JSON = """
            {
              "id": "resp_123",
              "object": "response",
              "status": "completed",
              "model": "gpt-test",
              "output": [
                { "type": "reasoning", "summary": [] },
                { "type": "message", "role": "assistant", "content": [ { "type": "output_text", "text": "Hi!" } ] }
              ]
            }
        """
    }

    private val messages =
        listOf(
            MessageData(ChatRole.USER, "hello"),
            MessageData(ChatRole.ASSISTANT, "hi"),
            MessageData(ChatRole.USER, "how are you?")
        )

    @Test
    fun `getReply posts full history to responses endpoint and returns output text`() = runBlocking {
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)

        val reply = ChatService("gpt-test", "sk-test", USER_ID, httpClient).getReply(messages, "you are a bot")

        assertEquals("Hi!", reply)
        assertEquals("/v1/responses", httpClient.requestPath)
        assertEquals("Bearer sk-test", httpClient.authHeader)

        val body = Json.parseToJsonElement(httpClient.requestBody).jsonObject
        assertEquals("gpt-test", body["model"]?.jsonPrimitive?.content)
        assertEquals(false, body["store"]?.jsonPrimitive?.boolean)

        val input = body["input"]!!.jsonArray
        assertEquals(messages.map { it.role }, input.map { it.jsonObject["role"]?.jsonPrimitive?.content })
        assertEquals(messages.map { it.content }, input.map { it.jsonObject["content"]?.jsonPrimitive?.content })
    }

    @Test
    fun `getReply sends the system prompt as instructions, out of the input`() = runBlocking {
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)

        ChatService("gpt-test", "sk-test", USER_ID, httpClient).getReply(messages, "you are a bot")

        val body = Json.parseToJsonElement(httpClient.requestBody).jsonObject
        assertEquals("you are a bot", body["instructions"]?.jsonPrimitive?.content)
        assertFalse(
            body["input"]!!.jsonArray.any { it.jsonObject["role"]?.jsonPrimitive?.content == ChatRole.SYSTEM },
            "the static prompt must not be duplicated in the input"
        )
    }

    @Test
    fun `getReply identifies the user by a hash, the same one between requests`() = runBlocking {
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)
        val service = ChatService("gpt-test", "sk-test", USER_ID, httpClient)

        service.getReply(messages, "you are a bot")
        val first = Json.parseToJsonElement(httpClient.requestBody).jsonObject

        service.getReply(messages, "you are a bot")
        val second = Json.parseToJsonElement(httpClient.requestBody).jsonObject

        val safetyIdentifier = assertNotNull(first["safety_identifier"]?.jsonPrimitive?.content)
        val promptCacheKey = assertNotNull(first["prompt_cache_key"]?.jsonPrimitive?.content)

        assertEquals(safetyIdentifier, second["safety_identifier"]?.jsonPrimitive?.content)
        assertEquals(promptCacheKey, second["prompt_cache_key"]?.jsonPrimitive?.content)

        assertFalse(safetyIdentifier.contains(USER_ID.toString()), "the telegram id must not be sent as is")
        assertTrue(safetyIdentifier.length <= 64, "safety_identifier is limited to 64 characters")
        assertEquals("chat-$safetyIdentifier", promptCacheKey)
    }

    @Test
    fun `every user gets an own cache key`() = runBlocking {
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)

        ChatService("gpt-test", "sk-test", USER_ID, httpClient).getReply(messages, "you are a bot")
        val first = Json.parseToJsonElement(httpClient.requestBody).jsonObject["prompt_cache_key"]?.jsonPrimitive?.content

        ChatService("gpt-test", "sk-test", USER_ID + 1, httpClient).getReply(messages, "you are a bot")
        val second = Json.parseToJsonElement(httpClient.requestBody).jsonObject["prompt_cache_key"]?.jsonPrimitive?.content

        assertNotEquals(first, second)
    }
}
