package com.helltar.aibot.database

import com.helltar.aibot.Config.creatorId
import com.helltar.aibot.Config.databaseName
import com.helltar.aibot.Config.databasePassword
import com.helltar.aibot.Config.databaseUser
import com.helltar.aibot.Config.postgresqlHost
import com.helltar.aibot.Config.postgresqlPort
import com.helltar.aibot.commandcore.CommandNames.toggleableCommands
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

    fun init() {
        val url = "r2dbc:postgresql://$postgresqlHost:$postgresqlPort/$databaseName"
        val database = R2dbcDatabase.connect(url, user = databaseUser, password = databasePassword)

        runBlocking {
            suspendTransaction(database) {
                createTables()
                createSudoUser()
                initializeCommands()
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
            CommandsStateTable, SlowmodeTable, SudoersTable, ConfigurationsTable, ChatHistory
        )
    }

    private suspend fun createSudoUser() {
        SudoersTable
            .insertIgnore {
                it[userId] = creatorId
                it[username] = "Owner"
                it[createdAt] = utcNow()
            }
    }

    private suspend fun initializeCommands() {
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
