package com.helltar.aibot.database.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.javatime.timestamp

object ChatHistoryTable : IntIdTable() {

    val userId = long("user_id").index()
    val role = varchar("role", 30).index()
    val content = text("content")

    val createdAt = timestamp("created_at")
}
