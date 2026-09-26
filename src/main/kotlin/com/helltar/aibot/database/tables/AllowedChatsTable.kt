package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object AllowedChatsTable : Table("allowed_chats") {

    val chatId = long("chat_id")
    val title = varchar("title", 70).nullable()
    val createdAt = timestampWithTimeZone("created_at").clientDefault { utcNow() }

    override val primaryKey = PrimaryKey(chatId)
}
