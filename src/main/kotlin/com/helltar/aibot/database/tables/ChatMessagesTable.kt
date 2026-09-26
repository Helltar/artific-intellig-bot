package com.helltar.aibot.database.tables

import com.helltar.aibot.database.utcNow
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

/** One row per message of a user's dialog history, read and trimmed in the order of [id]. */
object ChatMessagesTable : LongIdTable("chat_messages") {

    val userId = long("user_id")
    val role = varchar("role", 30)
    val content = text("content")
    val createdAt = timestampWithTimeZone("created_at").clientDefault { utcNow() }

    init {
        // every query takes the messages of one user ordered by id
        index("chat_messages_user_id_id", false, userId, id)
    }
}
