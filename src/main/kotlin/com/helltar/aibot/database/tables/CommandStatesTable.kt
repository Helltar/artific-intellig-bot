package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object CommandStatesTable : Table("command_states") {

    val commandName = varchar("command_name", 40)
    val isDisabled = bool("is_disabled")
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").clientDefault { utcNow() }

    override val primaryKey = PrimaryKey(commandName)
}
