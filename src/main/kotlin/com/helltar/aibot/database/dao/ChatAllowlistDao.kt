package com.helltar.aibot.database.dao

import com.helltar.aibot.database.CachedSet
import com.helltar.aibot.database.Database.dbTransaction
import com.helltar.aibot.database.fit
import com.helltar.aibot.database.models.ChatAllowlistData
import com.helltar.aibot.database.tables.AllowedChatsTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertIgnore
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

class ChatAllowlistDao {

    // checked on every command
    private val chatIds = CachedSet { dbTransaction { AllowedChatsTable.select(AllowedChatsTable.chatId).map { it[AllowedChatsTable.chatId] }.toList() } }

    // the row exists after an insert or an ignored duplicate alike, so the cache follows either way
    suspend fun add(chatId: Long, title: String?): Boolean =
        dbTransaction {
            AllowedChatsTable
                .insertIgnore {
                    it[this.chatId] = chatId
                    it[this.title] = title?.let(this.title::fit)
                }
                .insertedCount > 0
        }.also { chatIds.add(chatId) }

    suspend fun remove(chatId: Long): Boolean =
        dbTransaction {
            AllowedChatsTable
                .deleteWhere { this.chatId eq chatId } > 0
        }.also { chatIds.remove(chatId) }

    suspend fun list(): List<ChatAllowlistData> = dbTransaction {
        AllowedChatsTable
            .selectAll()
            .map {
                ChatAllowlistData(
                    it[AllowedChatsTable.chatId],
                    it[AllowedChatsTable.title],
                    it[AllowedChatsTable.createdAt].toInstant()
                )
            }.toList()
    }

    suspend fun contains(chatId: Long): Boolean =
        chatIds.contains(chatId)
}

val chatAllowlistDao = ChatAllowlistDao()
