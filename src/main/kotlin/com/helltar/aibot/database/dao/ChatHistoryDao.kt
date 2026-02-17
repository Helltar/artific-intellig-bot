package com.helltar.aibot.database.dao

import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.tables.ChatHistoryTable
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.DateTimeUtils.utcNow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import java.time.Instant

class ChatHistoryDao {

    suspend fun insert(userId: Long, mesasage: MessageData): Boolean = dbTransaction {
        ChatHistoryTable
            .insert {
                it[this.userId] = userId
                it[role] = mesasage.role
                it[content] = mesasage.content
                it[createdAt] = utcNow()
            }.insertedCount > 0
    }

    suspend fun loadHistory(userId: Long): List<Pair<MessageData, Instant>> = dbTransaction {
        ChatHistoryTable
            .select(ChatHistoryTable.role, ChatHistoryTable.content, ChatHistoryTable.createdAt)
            .where { ChatHistoryTable.userId eq userId }
            .orderBy(ChatHistoryTable.id)
            .map {
                MessageData(
                    it[ChatHistoryTable.role],
                    it[ChatHistoryTable.content]
                ) to it[ChatHistoryTable.createdAt]
            }.toList() // todo: flow
    }

    suspend fun deleteOldestEntry(userId: Long): Boolean = dbTransaction {
        val messageId =
            ChatHistoryTable
                .select(ChatHistoryTable.id)
                .where { ChatHistoryTable.userId eq userId and (ChatHistoryTable.role neq ChatRole.SYSTEM) }
                .orderBy(ChatHistoryTable.id)
                .limit(1)
                .singleOrNull()?.getOrNull(ChatHistoryTable.id)

        messageId?.let { ChatHistoryTable.deleteWhere { ChatHistoryTable.id eq messageId } > 0 } == true
    }

    suspend fun clearHistory(userId: Long): Boolean = dbTransaction {
        ChatHistoryTable
            .deleteWhere { ChatHistoryTable.userId eq userId } > 0
    }
}

val chatHistoryDao = ChatHistoryDao()
