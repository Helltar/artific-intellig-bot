import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MigrationTest {

    private companion object {
        val TABLES =
            listOf("admins", "allowed_chats", "banned_users", "chat_messages", "command_states", "schema_version", "settings", "slowmode_usage")

        // rows as the old version stored them: timestamps are the wall time of the zone the bot ran in
        val LEGACY_ROWS = """
            INSERT INTO sudoers VALUES (1, 'Owner', '2026-01-02 03:04:05');
            INSERT INTO bannedusers VALUES (42, 'spammer', 'Spam', 'ads', '2026-02-03 04:05:06');
            INSERT INTO chatallowlist VALUES (-100, 'group', '2026-01-02 03:04:05');
            INSERT INTO chathistory (user_id, role, content, created_at) VALUES (42, 'user', 'hello', '2026-03-04 05:06:07'), (42, 'assistant', 'hi', '2026-03-04 05:06:08');
            INSERT INTO commandsstate VALUES ('imgen', true, '2026-04-05 06:07:08', '2026-01-01 00:00:00'), ('chat', false, NULL, '2026-01-01 00:00:00');
            INSERT INTO configurations VALUES ('chat_model', 'gpt-test', NULL, '2026-01-01 00:00:00');
            INSERT INTO slowmode VALUES (42, 3, '2026-05-06 07:08:09', '2026-01-01 00:00:00');
            INSERT INTO apikeys VALUES ('openai', 'sk-old');
        """.trimIndent()
    }

    @BeforeTest
    fun docker() {
        TestPostgres.assumeDocker()
    }

    @Test
    fun `a new database gets the current schema and the latest version, without migrating`() = with(TestPostgres.current) {
        createDatabase("new_db")
        initBot("new_db")

        assertEquals(TABLES, tables("new_db"))
        assertEquals("1", psql("new_db", "SELECT string_agg(version::text, ',') FROM schema_version"))
    }

    @Test
    fun `an old database in a region zone is migrated to exactly the schema of a new one`() {
        migrateAndCompare(TestPostgres.current, "Europe/Kyiv")
    }

    @Test
    fun `an old database in a fixed offset zone is migrated to exactly the schema of a new one`() {
        /* postgres reads a bare offset with the sign reversed, this checks the migration does not */
        migrateAndCompare(TestPostgres.current, "GMT+05:30")
    }

    @Test
    fun `an old database on postgres 17 is migrated to exactly the schema of a new one`() {
        /* 17 has no named not null constraints, the migration must not depend on them */
        migrateAndCompare(TestPostgres.previous, "Europe/Kyiv")
    }

    @Test
    fun `a migrated database is not migrated again`() = with(TestPostgres.current) {
        createDatabase("twice_db")
        psqlFile("twice_db", "legacy-schema.sql")
        psql("twice_db", LEGACY_ROWS)

        initBot("twice_db")
        initBot("twice_db")

        assertEquals("1", psql("twice_db", "SELECT string_agg(version::text, ',') FROM schema_version"))
        assertEquals("2", psql("twice_db", "SELECT count(*) FROM chat_messages"))
    }

    private fun migrateAndCompare(postgres: TestPostgres, zoneId: String) = with(postgres) { withZone(zoneId) { migrateAndCheck(zoneId) } }

    private fun TestPostgres.migrateAndCheck(zoneId: String) {
        val suffix = zoneId.lowercase().filter { it.isLetterOrDigit() }
        val old = "old_$suffix"
        val new = "new_$suffix"

        createDatabase(old)
        psqlFile(old, "legacy-schema.sql")
        psql(old, LEGACY_ROWS)

        initBot(old)

        createDatabase(new)
        initBot(new)

        assertEquals(schema(new), schema(old), "a migrated database must have the schema of a new one")
        assertEquals(TABLES, tables(old), "the old tables must be renamed and the unused ones dropped")

        // the data survives, each timestamp is the same instant it was
        assertEquals(epoch("2026-02-03T04:05:06", zoneId), psql(old, "SELECT extract(epoch FROM banned_at)::bigint FROM banned_users WHERE user_id = 42").toLong())
        assertEquals(epoch("2026-05-06T07:08:09", zoneId), psql(old, "SELECT extract(epoch FROM last_used_at)::bigint FROM slowmode_usage WHERE user_id = 42").toLong())
        assertEquals("3", psql(old, "SELECT usage_count FROM slowmode_usage WHERE user_id = 42"))
        assertEquals("hello,hi", psql(old, "SELECT string_agg(content, ',' ORDER BY id) FROM chat_messages WHERE user_id = 42"))
        assertEquals("t", psql(old, "SELECT is_disabled FROM command_states WHERE command_name = 'imgen'"), "startup must not reset a disabled command")
        assertEquals("gpt-test", psql(old, "SELECT value FROM settings WHERE key = 'chat_model'"))
        assertEquals("1", psql(old, "SELECT count(*) FROM admins WHERE user_id = ${TestPostgres.CREATOR_ID}"))
        assertEquals("group", psql(old, "SELECT title FROM allowed_chats WHERE chat_id = -100"))

        // the sequence carries on from the old ids
        assertEquals("3", psql(old, "INSERT INTO chat_messages (user_id, role, content, created_at) VALUES (42, 'user', 'next', now()) RETURNING id"))
    }

    private fun TestPostgres.tables(database: String): List<String> =
        psql(database, "SELECT string_agg(tablename, ',' ORDER BY tablename) FROM pg_tables WHERE schemaname = 'public'").split(",")

    private fun epoch(wallTime: String, zoneId: String): Long =
        LocalDateTime.parse(wallTime).atZone(ZoneId.of(zoneId)).toEpochSecond()

    private fun withZone(zoneId: String, block: () -> Unit) {
        val default = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(zoneId)))

        try {
            block()
        } finally {
            TimeZone.setDefault(default)
        }
    }
}
