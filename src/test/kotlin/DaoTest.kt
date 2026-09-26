import com.helltar.aibot.database.dao.*
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.v1.r2dbc.transactions.TransactionManager
import org.telegram.telegrambots.meta.api.objects.User
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.*

class DaoTest {

    private companion object {
        val database by lazy {
            TestPostgres.current.run {
                createDatabase("dao_db")
                initBot("dao_db")
            }
        }

        /* the daos and their caches are shared, so every test takes ids of its own */
        val nextId = AtomicLong(1_000)
    }

    private val id = nextId.addAndGet(10)

    @BeforeTest
    fun connect() {
        TestPostgres.assumeDocker()
        TransactionManager.defaultDatabase = database // the daos run in the default database
    }

    @Test
    fun `the oldest history is deleted for one user only`() = runBlocking {
        repeat(10) { chatHistoryDao.insert(id, MessageData(ChatRole.USER, "m$it")) }
        repeat(3) { chatHistoryDao.insert(id + 1, MessageData(ChatRole.USER, "o$it")) }

        assertEquals(4, chatHistoryDao.deleteOldest(id, 4))
        assertEquals((4..9).map { "m$it" }, chatHistoryDao.loadHistory(id).map { it.first.content })

        assertEquals(6, chatHistoryDao.deleteOldest(id, 100))
        assertEquals(3, chatHistoryDao.loadHistory(id + 1).size, "another user's history must stay")
    }

    @Test
    fun `a setting is inserted and then updated`() = runBlocking {
        assertTrue(configurationsDao.updateChatModel("model-a"))
        assertTrue(configurationsDao.updateChatModel("model-b"))
        assertEquals("model-b", configurationsDao.chatModel())
    }

    @Test
    fun `the startup creates the owner and the commands, and keeps what an admin changed`() = runBlocking {
        assertTrue(sudoersDao.isAdmin(TestPostgres.CREATOR_ID))
        assertFalse(commandsDao.isDisabled("chat"))

        assertTrue(commandsDao.changeState("imgen", true))
        TestPostgres.current.initBot("dao_db") // a restart
        TransactionManager.defaultDatabase = database

        assertEquals(1, sudoersDao.list().count { it.userId == TestPostgres.CREATOR_ID })
        assertTrue(commandsDao.isDisabled("imgen"))
        assertTrue(commandsDao.changeState("imgen", false))
        assertFalse(commandsDao.isDisabled("imgen"))
        assertFalse(commandsDao.changeState("no-such-command", true))
        assertFalse(commandsDao.isDisabled("no-such-command"))
    }

    @Test
    fun `the admin check follows adds and removes`() = runBlocking {
        assertFalse(sudoersDao.isAdmin(id))

        assertTrue(sudoersDao.add(id, "admin"))
        assertFalse(sudoersDao.add(id, "admin"), "a second add must report nothing inserted")
        assertTrue(sudoersDao.isAdmin(id))

        assertTrue(sudoersDao.remove(id))
        assertFalse(sudoersDao.isAdmin(id))
    }

    @Test
    fun `the ban check follows bans and unbans`() = runBlocking {
        assertFalse(banlistDao.isBanned(id))

        assertTrue(banlistDao.ban(user(id), "spam"))
        assertTrue(banlistDao.isBanned(id))
        assertEquals("spam", banlistDao.reason(id))

        assertTrue(banlistDao.unban(id))
        assertFalse(banlistDao.isBanned(id))
        assertNull(banlistDao.reason(id))
    }

    @Test
    fun `the allowlist check follows adds and removes`() = runBlocking {
        val chatId = -id

        assertFalse(chatAllowlistDao.contains(chatId))

        assertTrue(chatAllowlistDao.add(chatId, "chat"))
        assertFalse(chatAllowlistDao.add(chatId, "chat"))
        assertTrue(chatAllowlistDao.contains(chatId))

        assertTrue(chatAllowlistDao.remove(chatId))
        assertFalse(chatAllowlistDao.contains(chatId))
    }

    @Test
    fun `free text is cut to its column instead of failing the insert`() = runBlocking {
        assertTrue(banlistDao.ban(user(id), "😀".repeat(300)))
        val reason = assertNotNull(banlistDao.reason(id))
        assertEquals(150, reason.codePointCount(0, reason.length))

        assertTrue(sudoersDao.add(id, "u".repeat(100)))
        assertEquals(32, sudoersDao.list().first { it.userId == id }.username?.length)
    }

    @Test
    fun `a timestamp reads back as the same instant in any time zone`() = runBlocking {
        assertTrue(banlistDao.ban(user(id), null))

        val bannedAt = banlistDao.list().first { it.userId == id }.bannedAt
        assertTrue(Duration.between(bannedAt, Instant.now()).abs() < Duration.ofMinutes(1))

        val default = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("Pacific/Kiritimati"))) // utc+14
            assertEquals(bannedAt, banlistDao.list().first { it.userId == id }.bannedAt)
        } finally {
            TimeZone.setDefault(default)
        }
    }

    private fun user(id: Long): User =
        User.builder().id(id).firstName("Tester").isBot(false).build()
}
