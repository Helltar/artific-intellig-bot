package com.helltar.aibot.chat

import com.helltar.aibot.openai.models.common.MessageData
import java.time.Instant

interface ChatHistoryStorage {

    suspend fun insert(userId: Long, message: MessageData): Boolean

    suspend fun loadHistory(userId: Long): List<Pair<MessageData, Instant>>

    // returns how many messages were deleted
    suspend fun deleteOldest(userId: Long, count: Int): Int

    suspend fun clearHistory(userId: Long): Boolean
}
