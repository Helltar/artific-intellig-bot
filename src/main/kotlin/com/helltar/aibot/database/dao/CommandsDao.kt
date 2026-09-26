package com.helltar.aibot.database.dao

import com.helltar.aibot.database.CachedSet
import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.tables.CommandStatesTable
import com.helltar.aibot.database.utcNow
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
                CommandStatesTable
                    .select(CommandStatesTable.commandName)
                    .where { CommandStatesTable.isDisabled eq true }
                    .map { it[CommandStatesTable.commandName] }
                    .toList()
            }
        }

    suspend fun changeState(command: String, disable: Boolean): Boolean =
        dbTransaction {
            CommandStatesTable
                .update({ CommandStatesTable.commandName eq command }) {
                    it[isDisabled] = disable
                    it[updatedAt] = utcNow()
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
