import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.chat.ChatHistoryStorage
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.*

/* In-memory replacement for ChatHistoryDao. The manager keeps user and assistant messages only,
   the system prompt is built per request by SystemPrompt and is never a part of the history. */
private class FakeChatHistoryStorage : ChatHistoryStorage {

    val stored = mutableListOf<MessageData>()
    var deleteCalls = 0

    override suspend fun insert(userId: Long, message: MessageData): Boolean {
        stored.add(message)
        return true
    }

    override suspend fun loadHistory(userId: Long): List<Pair<MessageData, Instant>> =
        stored.map { it to Instant.now() }

    override suspend fun deleteOldest(userId: Long, count: Int): Int {
        deleteCalls++
        val deleted = count.coerceAtMost(stored.size)
        stored.subList(0, deleted).clear()
        return deleted
    }

    override suspend fun clearHistory(userId: Long): Boolean {
        stored.clear()
        return true
    }
}

class ChatHistoryManagerTest {

    private companion object {
        /* ChatHistoryManager keeps a static per-userId context map, so every test gets a fresh userId */
        val nextUserId = AtomicLong(100_000)
    }

    private val userId = nextUserId.incrementAndGet()
    private val storage = FakeChatHistoryStorage()
    private val manager = ChatHistoryManager(userId, storage)

    @Test
    fun `dialog keeps user and assistant messages only`() = runBlocking {
        manager.saveUserMessage("one")
        manager.saveAssistantMessage("reply")
        manager.saveUserMessage("two")

        val messages = manager.messages()
        assertEquals(listOf(ChatRole.USER, ChatRole.ASSISTANT, ChatRole.USER), messages.map { it.role })
        assertEquals(listOf("one", "reply", "two"), messages.map { it.content })
        assertEquals(messages.map { it.content }, storage.stored.map { it.content }, "everything must be persisted")
    }

    @Test
    fun `history within the token budget is not trimmed`() = runBlocking {
        repeat(3) {
            manager.saveUserMessage("u".repeat(10_000))
            manager.saveAssistantMessage("a".repeat(10_000))
        }

        /* the history is far over the old 24576 characters limit, the budget is counted in tokens now */
        manager.fitTokenBudget(requestTokens = 20_000, requestChars = 61_000)

        assertEquals(6, manager.messages().size)
        assertEquals(0, storage.deleteCalls)
    }

    @Test
    fun `history over the token budget is cut to about a half at once`() = runBlocking {
        repeat(10) { i ->
            manager.saveUserMessage("question $i " + "u".repeat(1_990))
            manager.saveAssistantMessage("answer $i " + "a".repeat(1_990))
        }

        /* 20 messages of ~2000 chars at 1 token per char: 40k tokens, over the 32k budget */
        val historyChars = manager.messages().sumOf { it.content.length }
        manager.fitTokenBudget(requestTokens = historyChars, requestChars = historyChars)

        val messages = manager.messages()
        val tokensLeft = messages.sumOf { it.content.length }

        assertTrue(tokensLeft <= 16_000, "the history must be cut to half of the budget, left $tokensLeft")
        assertTrue(tokensLeft > 12_000, "the cut must not go much further than half of the budget, left $tokensLeft")
        assertEquals(ChatRole.USER, messages.first().role, "an assistant reply must not become the first message")
        assertTrue(messages.last().content.startsWith("answer 9"), "the latest messages must be kept")
        assertEquals(messages.map { it.content }, storage.stored.map { it.content }, "trimming must also delete from storage")
        assertEquals(1, storage.deleteCalls, "the whole cut must be one delete")
    }

    @Test
    fun `the cut follows the tokens per character of the request`() = runBlocking {
        repeat(10) {
            manager.saveUserMessage("u".repeat(1_000))
            manager.saveAssistantMessage("a".repeat(1_000))
        }

        /* 2 tokens per char, as in a dense language: 16k tokens is only 8000 chars of history */
        manager.fitTokenBudget(requestTokens = 40_000, requestChars = 20_000)

        assertEquals(8_000, manager.messages().sumOf { it.content.length })
    }

    @Test
    fun `a single message over the budget does not leave an assistant reply first`() = runBlocking {
        manager.saveUserMessage("u".repeat(40_000))
        manager.saveAssistantMessage("a".repeat(100))

        manager.fitTokenBudget(requestTokens = 40_100, requestChars = 40_100)

        assertTrue(manager.messages().isEmpty())
        assertTrue(storage.stored.isEmpty())
    }

    @Test
    fun `clear empties history`() = runBlocking {
        manager.saveUserMessage("hello")
        manager.saveAssistantMessage("hi")

        assertTrue(manager.clear())
        assertTrue(manager.messages().isEmpty())
        assertTrue(storage.stored.isEmpty())

        manager.saveUserMessage("again")
        assertEquals(listOf("again"), manager.messages().map { it.content })
    }

    @Test
    fun `existing history is loaded from storage`() = runBlocking {
        storage.stored += MessageData(ChatRole.USER, "old question")
        storage.stored += MessageData(ChatRole.ASSISTANT, "old answer")

        assertEquals(listOf("old question", "old answer"), manager.messages().map { it.content })
    }

    @Test
    fun `history keeps the time of every message`() = runBlocking {
        manager.saveUserMessage("question")
        manager.saveAssistantMessage("answer")

        val history = manager.history()
        assertEquals(2, history.size)
        assertTrue(history.all { it.second <= Instant.now() })
    }
}
