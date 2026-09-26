package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object AdminsTable : Table("admins") {

    val userId = long("user_id")
    val username = varchar("username", 32).nullable()
    val createdAt = timestampWithTimeZone("created_at").clientDefault { utcNow() }

    override val primaryKey = PrimaryKey(userId)
}
