package com.helltar.aibot.database.tables

import com.helltar.aibot.utils.DateTimeUtils.instantNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object ApiKeysTable : Table() {

    val provider = varchar("provider", 40)
    val apiKey = varchar("api_key", 150)
    val updatedAt = timestamp("updated_at").nullable()
    val createdAt = timestamp("created_at").clientDefault { instantNow() }

    override val primaryKey = PrimaryKey(provider)
}
