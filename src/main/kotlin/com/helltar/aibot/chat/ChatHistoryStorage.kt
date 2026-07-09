package com.helltar.aibot.chat

import com.helltar.aibot.openai.models.common.MessageData
import java.time.Instant

interface ChatHistoryStorage {

    suspend fun insert(userId: Long, message: MessageData): Boolean

    suspend fun loadHistory(userId: Long): List<Pair<MessageData, Instant>>

    suspend fun deleteOldestEntry(userId: Long): Boolean

    suspend fun clearHistory(userId: Long): Boolean
}
