package com.helltar.aibot.database

import com.helltar.aibot.Config
import com.helltar.aibot.database.Migrations.migrate
import com.helltar.aibot.database.tables.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object Database {

    fun init(config: Config.BotConfig, toggleableCommands: List<String>) {
        // pooled: without a pool every transaction opens a new connection to postgres; a small bot needs only a few
        val url = "r2dbc:pool:postgresql://${config.postgresqlHost}:5432/${config.databaseName}?initialSize=1&maxSize=5"
        init(url, config.databaseUser, config.databasePassword, config.creatorId, toggleableCommands)
    }

    // takes the url itself, so the tests can point it at a database of their own
    internal fun init(url: String, user: String, password: String, creatorId: Long, toggleableCommands: List<String>): R2dbcDatabase {
        val database = R2dbcDatabase.connect(url, user = user, password = password)

        // one transaction: a failed migration leaves the database as it was, postgres rolls ddl back too
        runBlocking {
            suspendTransaction(database) {
                migrate()
                createTables()
                createSudoUser(creatorId)
                initializeCommands(toggleableCommands)
            }
        }

        return database
    }

    suspend fun <T> dbTransaction(block: suspend R2dbcTransaction.() -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }

    private suspend fun createTables() {
        SchemaUtils.create(
            AdminsTable, AllowedChatsTable, BannedUsersTable, ChatMessagesTable,
            CommandStatesTable, SettingsTable, SlowmodeUsageTable
        )
    }

    private suspend fun createSudoUser(creatorId: Long) {
        AdminsTable
            .insertIgnore {
                it[userId] = creatorId
                it[username] = "Owner"
            }
    }

    // ignore: a command already known keeps the state an admin gave it
    private suspend fun initializeCommands(toggleableCommands: List<String>) {
        CommandStatesTable.batchInsert(toggleableCommands, ignore = true, shouldReturnGeneratedValues = false) {
            this[CommandStatesTable.commandName] = it
            this[CommandStatesTable.isDisabled] = false
        }
    }
}
