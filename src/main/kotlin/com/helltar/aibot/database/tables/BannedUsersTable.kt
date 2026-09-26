package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object BannedUsersTable : Table("banned_users") {

    val userId = long("user_id")
    val username = varchar("username", 32).nullable()
    val firstName = varchar("first_name", 64)
    val reason = varchar("reason", 150).nullable()
    val bannedAt = timestampWithTimeZone("banned_at").clientDefault { utcNow() }

    override val primaryKey = PrimaryKey(userId)
}
