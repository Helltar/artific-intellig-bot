package com.helltar.aibot.database.dao

import com.helltar.aibot.chat.ChatHistoryStorage
import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.tables.ChatMessagesTable
import com.helltar.aibot.openai.models.common.MessageData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import java.time.Instant

class ChatHistoryDao : ChatHistoryStorage {

    override suspend fun insert(userId: Long, message: MessageData): Boolean = dbTransaction {
        ChatMessagesTable
            .insert {
                it[this.userId] = userId
                it[role] = message.role
                it[content] = message.content
            }.insertedCount > 0
    }

    // a list, not a flow: an r2dbc query runs only while it is collected inside its transaction, and the manager keeps the whole history in memory anyway
    override suspend fun loadHistory(userId: Long): List<Pair<MessageData, Instant>> = dbTransaction {
        ChatMessagesTable
            .select(ChatMessagesTable.role, ChatMessagesTable.content, ChatMessagesTable.createdAt)
            .where { ChatMessagesTable.userId eq userId }
            .orderBy(ChatMessagesTable.id)
            .map {
                MessageData(
                    it[ChatMessagesTable.role],
                    it[ChatMessagesTable.content]
                ) to it[ChatMessagesTable.createdAt].toInstant()
            }.toList()
    }

    // one statement for the whole cut: delete ... where id in (select ... order by id limit count)
    override suspend fun deleteOldest(userId: Long, count: Int): Int = dbTransaction {
        val oldest =
            ChatMessagesTable
                .select(ChatMessagesTable.id)
                .where { ChatMessagesTable.userId eq userId }
                .orderBy(ChatMessagesTable.id)
                .limit(count)

        ChatMessagesTable.deleteWhere { id inSubQuery oldest }
    }

    override suspend fun clearHistory(userId: Long): Boolean = dbTransaction {
        ChatMessagesTable
            .deleteWhere { ChatMessagesTable.userId eq userId } > 0
    }
}

val chatHistoryDao = ChatHistoryDao()
