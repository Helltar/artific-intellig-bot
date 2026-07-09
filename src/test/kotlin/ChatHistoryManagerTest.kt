import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.chat.ChatHistoryStorage
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.*

/* In-memory replacement for ChatHistoryDao. Note: the system prompt is never persisted
   by ChatHistoryManager (it lives only in the in-memory context), so `stored` holds
   user/assistant messages only — same as the real database. */
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

    private fun telegramMessage(): Message =
        Message().apply {
            from = User.builder().id(userId).firstName("Tester").userName("tester").isBot(false).build()
            chat = Chat.builder().id(userId).type("private").build()
        }

    @Test
    fun `first user message gets system prompt prepended`() = runBlocking {
        manager.saveUserMessage(telegramMessage(), "hello")

        val messages = manager.messages()
        assertEquals(listOf(ChatRole.SYSTEM, ChatRole.USER), messages.map { it.role })
        assertEquals("hello", messages[1].content)
        assertFalse(messages[0].content.contains("{user_name}"), "placeholders must be substituted")
        assertFalse(messages[0].content.contains("{room_name}"))
        assertFalse(messages[0].content.contains("{user_id}"))

        assertEquals(manager.messages()[0].content, manager.systemPrompt())
        assertEquals(listOf("hello"), storage.stored.map { it.content }, "system prompt must not be persisted")
    }

    @Test
    fun `system prompt is not duplicated on subsequent messages`() = runBlocking {
        manager.saveUserMessage(telegramMessage(), "one")
        manager.saveAssistantMessage("reply")
        manager.saveUserMessage(telegramMessage(), "two")

        assertEquals(
            listOf(ChatRole.SYSTEM, ChatRole.USER, ChatRole.ASSISTANT, ChatRole.USER),
            manager.messages().map { it.role }
        )
    }

    @Test
    fun `history over length limit is trimmed keeping system prompt and dropping orphan assistant messages`() = runBlocking {
        /* two big messages together exceed MAX_DIALOG_HISTORY_LENGTH (24576 chars) */
        manager.saveUserMessage(telegramMessage(), "u".repeat(20_000))
        manager.saveAssistantMessage("a".repeat(20_000))
        manager.saveUserMessage(telegramMessage(), "latest question")

        val messages = manager.messages()
        assertEquals(listOf(ChatRole.SYSTEM, ChatRole.USER), messages.map { it.role }, "old pair must be dropped, assistant reply must not become the first dialog message")
        assertEquals("latest question", messages[1].content)
        assertEquals(listOf("latest question"), storage.stored.map { it.content }, "trimming must also delete from storage")
    }

    @Test
    fun `history under length limit is not trimmed`() = runBlocking {
        manager.saveUserMessage(telegramMessage(), "u".repeat(10_000))
        manager.saveAssistantMessage("a".repeat(10_000))
        manager.saveUserMessage(telegramMessage(), "next")

        assertEquals(4, manager.messages().size)
    }

    @Test
    fun `clear empties history and system prompt is re-added on next message`() = runBlocking {
        manager.saveUserMessage(telegramMessage(), "hello")
        manager.saveAssistantMessage("hi")

        assertTrue(manager.clear())
        assertTrue(manager.messages().isEmpty())
        assertTrue(storage.stored.isEmpty())
        assertNull(manager.systemPrompt())

        manager.saveUserMessage(telegramMessage(), "again")
        assertEquals(listOf(ChatRole.SYSTEM, ChatRole.USER), manager.messages().map { it.role })
    }

    @Test
    fun `existing history is loaded from storage`() = runBlocking {
        storage.stored += MessageData(ChatRole.USER, "old question")
        storage.stored += MessageData(ChatRole.ASSISTANT, "old answer")

        assertEquals(listOf("old question", "old answer"), manager.messages().map { it.content })
    }
}
