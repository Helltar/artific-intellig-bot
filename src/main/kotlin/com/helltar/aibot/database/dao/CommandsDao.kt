package com.helltar.aibot.database.dao

import com.helltar.aibot.database.CachedSet
import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.tables.CommandsStateTable
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update

class CommandsDao {

    // checked on every command
    private val disabledCommands =
        CachedSet {
            dbTransaction {
                CommandsStateTable
                    .select(CommandsStateTable.commandName)
                    .where { CommandsStateTable.isDisabled eq true }
                    .map { it[CommandsStateTable.commandName] }
                    .toList()
            }
        }

    suspend fun changeState(command: String, disable: Boolean): Boolean =
        dbTransaction {
            CommandsStateTable
                .update({ CommandsStateTable.commandName eq command }) {
                    it[isDisabled] = disable
                    it[updatedAt] = instantNow()
                } > 0
        }.also { updated ->
            if (updated) {
                if (disable) disabledCommands.add(command) else disabledCommands.remove(command)
            }
        }

    suspend fun isDisabled(command: String): Boolean =
        disabledCommands.contains(command)
}

val commandsDao = CommandsDao()
