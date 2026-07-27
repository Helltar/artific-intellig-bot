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

    override suspend fun insert(userId: Long, message: MessageData): Boolean {
        stored.add(message)
        return true
    }

    override suspend fun loadHistory(userId: Long): List<Pair<MessageData, Instant>> =
        stored.map { it to Instant.now() }

    override suspend fun deleteOldestEntry(userId: Long): Boolean {
        if (stored.isEmpty()) return false
        stored.removeAt(0)
        return true
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
    fun `history over length limit is trimmed dropping orphan assistant messages`() = runBlocking {
        /* two big messages together exceed MAX_DIALOG_HISTORY_LENGTH (24576 chars) */
        manager.saveUserMessage("u".repeat(20_000))
        manager.saveAssistantMessage("a".repeat(20_000))
        manager.saveUserMessage("latest question")

        val messages = manager.messages()
        assertEquals(listOf(ChatRole.USER), messages.map { it.role }, "old pair must be dropped, an assistant reply must not become the first message")
        assertEquals("latest question", messages[0].content)
        assertEquals(listOf("latest question"), storage.stored.map { it.content }, "trimming must also delete from storage")
    }

    @Test
    fun `history under length limit is not trimmed`() = runBlocking {
        manager.saveUserMessage("u".repeat(10_000))
        manager.saveAssistantMessage("a".repeat(10_000))
        manager.saveUserMessage("next")

        assertEquals(3, manager.messages().size)
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
