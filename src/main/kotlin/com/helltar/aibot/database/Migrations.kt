package com.helltar.aibot.database

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.exists
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Brings a database created by an older version of the bot to the current schema.
 *
 * `SchemaUtils.create` only creates the tables that are missing: a renamed table or a changed column
 * never reaches an existing database, which would get a new empty table next to the old one instead.
 * Such changes are written here as SQL, applied once, in order, and recorded in `schema_version`.
 * They run in the transaction of the startup, so a failed one leaves the database as it was.
 *
 * A new database gets the current schema from `SchemaUtils.create` and is only marked with the latest version.
 */
internal object Migrations {

    private val log = KotlinLogging.logger {}

    private object SchemaVersionTable : Table("schema_version") {
        val version = integer("version")
        val appliedAt = timestampWithTimeZone("applied_at").clientDefault { utcNow() }
        override val primaryKey = PrimaryKey(version)
    }

    // a table every database made before the migrations has, and no later one
    private object LegacyChatHistoryTable : Table("chathistory")

    private class Migration(val version: Int, val description: String, val statements: () -> List<String>)

    // append only: a released migration never changes, a new change is a new version
    private val migrations =
        listOf(
            Migration(1, "snake_case table names, timestamptz, chat message index, drop unused tables") { snakeCaseAndTimestamptz() }
        )

    suspend fun R2dbcTransaction.migrate() {
        SchemaUtils.create(SchemaVersionTable)

        val current = currentVersion() ?: baseline()

        migrations
            .filter { it.version > current }
            .forEach { migration ->
                log.info { "migrating the database to version ${migration.version}: ${migration.description}" }
                migration.statements().forEach { exec(it) }
                SchemaVersionTable.insert { it[version] = migration.version }
            }
    }

    private suspend fun currentVersion(): Int? =
        SchemaVersionTable
            .select(SchemaVersionTable.version.max())
            .singleOrNull()
            ?.get(SchemaVersionTable.version.max())

    // a database without a version is either new, or from before the migrations and still to be migrated
    private suspend fun baseline(): Int =
        if (LegacyChatHistoryTable.exists())
            0
        else {
            val latest = migrations.last().version
            SchemaVersionTable.insert { it[version] = latest }
            latest
        }

    private fun snakeCaseAndTimestamptz(): List<String> =
        buildList {
            addAll(renameTable("sudoers", "admins"))
            add(timestamptz("admins", "created_at"))

            addAll(renameTable("chatallowlist", "allowed_chats"))
            add(timestamptz("allowed_chats", "created_at"))

            addAll(renameTable("bannedusers", "banned_users"))
            add(timestamptz("banned_users", "banned_at"))

            addAll(renameTable("commandsstate", "command_states"))
            add(timestamptz("command_states", "updated_at", "created_at"))

            addAll(renameTable("configurations", "settings"))
            add(timestamptz("settings", "updated_at", "created_at"))

            add("ALTER TABLE slowmode RENAME COLUMN updated_at TO last_used_at")
            addAll(renameTable("slowmode", "slowmode_usage"))
            add(timestamptz("slowmode_usage", "last_used_at", "created_at"))

            addAll(renameTable("chathistory", "chat_messages"))
            add("ALTER SEQUENCE chathistory_id_seq RENAME TO chat_messages_id_seq")
            add("ALTER TABLE chat_messages ALTER COLUMN id TYPE BIGINT")
            add("ALTER SEQUENCE chat_messages_id_seq AS BIGINT")
            // the role index was never used, and every query takes one user's messages ordered by id
            add("DROP INDEX chathistory_role")
            add("DROP INDEX chathistory_user_id")
            add("CREATE INDEX chat_messages_user_id_id ON chat_messages (user_id, id)")
            add(timestamptz("chat_messages", "created_at"))

            // left behind by features removed long ago; apikeys still holds the openai key that moved to the environment
            add("DROP TABLE IF EXISTS apikeys, privacypolicies, files, globalslowmode, chatwhitelist")
        }

    // the constraints keep the names they got from the old table, a new database names them after the new one
    private fun renameTable(old: String, new: String): List<String> =
        listOf(
            "ALTER TABLE $old RENAME TO $new",
            "ALTER TABLE $new RENAME CONSTRAINT ${old}_pkey TO ${new}_pkey",
            renameNotNullConstraints(new)
        )

    /*
     * since postgres 18 a not null is a constraint of its own, named <table>_<column>_not_null; a renamed
     * table or column keeps the old name. postgres 17 has no such constraints, so the loop finds nothing there
     */
    private fun renameNotNullConstraints(table: String): String =
        """
        DO ${'$'}${'$'}
        DECLARE c record;
        BEGIN
            FOR c IN
                SELECT con.conname, att.attname
                FROM pg_constraint con
                JOIN pg_attribute att ON att.attrelid = con.conrelid AND att.attnum = con.conkey[1]
                WHERE con.conrelid = '$table'::regclass AND con.contype = 'n'
            LOOP
                IF c.conname <> '${table}_' || c.attname || '_not_null' THEN
                    EXECUTE format('ALTER TABLE $table RENAME CONSTRAINT %I TO %I', c.conname, '${table}_' || c.attname || '_not_null');
                END IF;
            END LOOP;
        END
        ${'$'}${'$'}
        """.trimIndent()

    private fun timestamptz(table: String, vararg columns: String): String =
        "ALTER TABLE $table " + columns.joinToString { "ALTER COLUMN $it TYPE TIMESTAMP WITH TIME ZONE USING ${wallTimeToInstant(it)}" }

    /*
     * the old timestamp columns hold the wall time of the zone the bot wrote them in, which is the zone
     * of the jvm, the same one it runs in now; a zone of a fixed offset is shifted by hand, because
     * postgres reads a bare offset like '+03:00' in the posix convention, with the sign reversed
     */
    private fun wallTimeToInstant(column: String): String =
        when (val zone = ZoneId.systemDefault().normalized()) {
            is ZoneOffset -> "($column - INTERVAL '${zone.totalSeconds} seconds') AT TIME ZONE 'UTC'"
            else -> "$column AT TIME ZONE '${zone.id}'"
        }
}
