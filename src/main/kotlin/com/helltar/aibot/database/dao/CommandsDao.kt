package com.helltar.aibot.database.dao

import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.tables.CommandsStateTable
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update

class CommandsDao {

    suspend fun changeState(command: String, disable: Boolean): Int = dbTransaction {
        CommandsStateTable
            .update({ CommandsStateTable.commandName eq command }) {
                it[isDisabled] = disable
                it[updatedAt] = instantNow()
            }
    }

    suspend fun isDisabled(command: String): Boolean = dbTransaction {
        CommandsStateTable
            .select(CommandsStateTable.isDisabled)
            .where { CommandsStateTable.commandName eq command }
            .singleOrNull()?.getOrNull(CommandsStateTable.isDisabled) == true
    }
}

val commandsDao = CommandsDao()
