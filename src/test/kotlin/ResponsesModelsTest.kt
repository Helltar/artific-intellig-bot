import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.ApiConfig.InputContentType
import com.helltar.aibot.openai.KtorHttpClient
import com.helltar.aibot.openai.models.common.ContentPartData
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.models.image.VisionMessageData
import com.helltar.aibot.openai.models.image.VisionRequestData
import com.helltar.aibot.openai.models.responses.ResponsesRequestData
import com.helltar.aibot.openai.models.responses.ResponsesResponseData
import kotlinx.serialization.json.*
import kotlin.test.*

class ResponsesModelsTest {

    /* the exact Json instance used by KtorHttpClient, so tests verify the real wire format */
    private val json = KtorHttpClient.json

    @Test
    fun `chat request serializes to responses api format`() {
        val request =
            ResponsesRequestData(
                model = "gpt-test",
                input = listOf(
                    MessageData(ChatRole.USER, "hello"),
                    MessageData(ChatRole.SYSTEM, "# Context")
                ),
                instructions = "you are a bot",
                promptCacheKey = "chat-abc",
                safetyIdentifier = "abc"
            )

        val body = json.encodeToJsonElement(ResponsesRequestData.serializer(), request).jsonObject

        assertEquals("gpt-test", body["model"]?.jsonPrimitive?.content)
        assertEquals(false, body["store"]?.jsonPrimitive?.boolean, "store=false must be sent explicitly")
        assertEquals("you are a bot", body["instructions"]?.jsonPrimitive?.content)
        assertEquals("chat-abc", body["prompt_cache_key"]?.jsonPrimitive?.content)
        assertEquals("abc", body["safety_identifier"]?.jsonPrimitive?.content)
        assertNull(body["messages"], "legacy chat completions field must not be present")
        assertNull(body["user"], "user is replaced by prompt_cache_key and safety_identifier")

        val input = assertNotNull(body["input"]).jsonArray
        assertEquals(2, input.size)
        assertEquals("user", input[0].jsonObject["role"]?.jsonPrimitive?.content)
        assertEquals("hello", input[0].jsonObject["content"]?.jsonPrimitive?.content)
        assertEquals("system", input[1].jsonObject["role"]?.jsonPrimitive?.content)
        assertEquals("# Context", input[1].jsonObject["content"]?.jsonPrimitive?.content)
    }

    @Test
    fun `optional request fields are omitted when not set`() {
        val request = ResponsesRequestData("gpt-test", listOf(MessageData(ChatRole.USER, "hello")))
        val body = json.encodeToJsonElement(ResponsesRequestData.serializer(), request).jsonObject

        assertNull(body["instructions"])
        assertNull(body["prompt_cache_key"])
        assertNull(body["safety_identifier"])
    }

    @Test
    fun `vision request uses input_text and input_image content parts`() {
        val request =
            VisionRequestData(
                "gpt-test",
                listOf(
                    VisionMessageData(
                        ChatRole.USER,
                        listOf(
                            ContentPartData(InputContentType.TEXT, "what is this?"),
                            ContentPartData(InputContentType.IMAGE, imageUrl = "data:image/jpeg;base64,AAAA")
                        )
                    )
                )
            )

        val body = json.encodeToJsonElement(VisionRequestData.serializer(), request).jsonObject
        val content = assertNotNull(body["input"]).jsonArray[0].jsonObject["content"]!!.jsonArray

        val textPart = content[0].jsonObject
        assertEquals("input_text", textPart["type"]?.jsonPrimitive?.content)
        assertEquals("what is this?", textPart["text"]?.jsonPrimitive?.content)
        assertNull(textPart["image_url"], "null fields must be omitted from json")

        val imagePart = content[1].jsonObject
        assertEquals("input_image", imagePart["type"]?.jsonPrimitive?.content)
        assertEquals("data:image/jpeg;base64,AAAA", imagePart["image_url"]?.jsonPrimitive?.content, "image_url must be a plain string, not an object")
        assertNull(imagePart["text"])
    }

    @Test
    fun `response parsing extracts text and skips reasoning items`() {
        /* realistic payload: reasoning item before the message, plus fields the models don't declare */
        val payload = """
            {
              "id": "resp_123",
              "object": "response",
              "created_at": 1741476542,
              "status": "completed",
              "model": "gpt-test",
              "output": [
                { "type": "reasoning", "id": "rs_1", "summary": [] },
                {
                  "type": "message",
                  "id": "msg_1",
                  "status": "completed",
                  "role": "assistant",
                  "content": [ { "type": "output_text", "text": "Hello there!", "annotations": [] } ]
                }
              ],
              "parallel_tool_calls": true,
              "previous_response_id": null,
              "store": false,
              "usage": {
                "input_tokens": 36,
                "input_tokens_details": { "cached_tokens": 0 },
                "output_tokens": 87,
                "output_tokens_details": { "reasoning_tokens": 0 },
                "total_tokens": 123
              }
            }
        """

        val response = json.decodeFromString<ResponsesResponseData>(payload)

        assertEquals("Hello there!", response.outputText())
        assertEquals(36, response.usage?.inputTokens)
        assertEquals(87, response.usage?.outputTokens)
        assertEquals(123, response.usage?.totalTokens)
    }

    @Test
    fun `outputText joins multiple text parts and messages`() {
        val payload = """
            {
              "model": "gpt-test",
              "output": [
                { "type": "message", "role": "assistant", "content": [
                  { "type": "output_text", "text": "part one, " },
                  { "type": "refusal", "refusal": "ignored" },
                  { "type": "output_text", "text": "part two" }
                ] }
              ]
            }
        """

        assertEquals("part one, part two", json.decodeFromString<ResponsesResponseData>(payload).outputText())
    }

    @Test
    fun `outputText is empty when output contains no message items`() {
        val payload = """{ "model": "gpt-test", "output": [ { "type": "reasoning", "summary": [] } ] }"""
        assertEquals("", json.decodeFromString<ResponsesResponseData>(payload).outputText())
    }
}
