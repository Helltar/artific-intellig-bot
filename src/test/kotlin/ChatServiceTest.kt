import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.service.ChatService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ChatServiceTest {

    private companion object {
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

    @Test
    fun `getReply posts full history to responses endpoint and returns output text`() = runBlocking {
        val httpClient = FakeOpenAiHttpClient(RESPONSE_JSON)
        val messages =
            listOf(
                MessageData(ChatRole.SYSTEM, "you are a bot"),
                MessageData(ChatRole.USER, "hello"),
                MessageData(ChatRole.ASSISTANT, "hi"),
                MessageData(ChatRole.USER, "how are you?")
            )

        val reply = ChatService("gpt-test", "sk-test", httpClient).getReply(messages)

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
}
