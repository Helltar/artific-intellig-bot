package com.helltar.aibot.database.tables

import com.helltar.aibot.utils.DateTimeUtils.instantNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object BannedUsersTable : Table() {

    val userId = long("user_id")
    val username = varchar("username", 32).nullable()
    val firstName = varchar("first_name", 64)
    val reason = varchar("reason", 150).nullable()
    val bannedAt = timestamp("banned_at").clientDefault { instantNow() }

    override val primaryKey = PrimaryKey(userId)
}
