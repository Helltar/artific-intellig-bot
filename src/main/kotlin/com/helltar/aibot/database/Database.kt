package com.helltar.aibot.database

import com.helltar.aibot.Config
import com.helltar.aibot.database.tables.*
import com.helltar.aibot.utils.DateTimeUtils.utcNow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcTransaction
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction

object Database {

    fun init(config: Config.BotConfig, toggleableCommands: List<String>) {
        val url = "r2dbc:postgresql://${config.postgresqlHost}:${config.postgresqlPort}/${config.databaseName}"
        val database = R2dbcDatabase.connect(url, user = config.databaseUser, password = config.databasePassword)

        runBlocking {
            suspendTransaction(database) {
                createTables()
                createSudoUser(config.creatorId)
                initializeCommands(toggleableCommands)
            }
        }
    }

    suspend fun <T> dbTransaction(block: suspend R2dbcTransaction.() -> T): T =
        withContext(Dispatchers.IO) {
            suspendTransaction { block() }
        }

    private suspend fun createTables() {
        SchemaUtils.create(
            ApiKeysTable, BannedUsersTable, ChatAllowlistTable,
            CommandsStateTable, SlowmodeTable, SudoersTable, ConfigurationsTable, ChatHistoryTable
        )
    }

    private suspend fun createSudoUser(creatorId: Long) {
        SudoersTable
            .insertIgnore {
                it[userId] = creatorId
                it[username] = "Owner"
                it[createdAt] = utcNow()
            }
    }

    private suspend fun initializeCommands(toggleableCommands: List<String>) {
        toggleableCommands.forEach { command ->
            CommandsStateTable
                .insertIgnore { // todo: batchInsert
                    it[commandName] = command
                    it[isDisabled] = false
                    it[createdAt] = utcNow()
                }
        }
    }
}
