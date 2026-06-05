package com.helltar.aibot.database.tables

import com.helltar.aibot.utils.DateTimeUtils.instantNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object ChatAllowlistTable : Table() {

    val chatId = long("chat_id")
    val title = varchar("title", 70).nullable()
    val createdAt = timestamp("created_at").clientDefault { instantNow() }

    override val primaryKey = PrimaryKey(chatId)
}
