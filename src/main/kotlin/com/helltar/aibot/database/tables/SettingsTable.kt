package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object SettingsTable : Table("settings") {

    val key = varchar("key", 50)
    val value = varchar("value", 250)
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
    val createdAt = timestampWithTimeZone("created_at").clientDefault { utcNow() }

    override val primaryKey = PrimaryKey(key)
}
