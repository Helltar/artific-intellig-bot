package com.helltar.aibot.chat

import com.helltar.aibot.database.dao.chatHistoryDao
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps the dialog history of a single user: user and assistant messages only.
 * The system prompt is not a part of it, it is built per request by [SystemPrompt].
 */
class ChatHistoryManager(private val userId: Long, private val storage: ChatHistoryStorage = chatHistoryDao) {

    private companion object {
        const val MAX_DIALOG_HISTORY_LENGTH = 24576 // todo: tokens
        val userChatContextMap = ConcurrentHashMap<Long, MutableList<Pair<MessageData, Instant>>>()
        val userLocks = ConcurrentHashMap<Long, Mutex>()
    }

    suspend fun history(): List<Pair<MessageData, Instant>> = withUserLock {
        chatContext().toList()
    }

    suspend fun messages(): List<MessageData> = withUserLock {
        chatContext().map { it.first }
    }

    suspend fun saveAssistantMessage(message: String): Unit = withUserLock {
        saveMessage(MessageData(ChatRole.ASSISTANT, message))
    }

    suspend fun saveUserMessage(messageText: String) = withUserLock {
        saveMessage(MessageData(ChatRole.USER, messageText))
        ensureDialogLengthWithinLimit()
    }

    suspend fun clear(): Boolean = withUserLock {
        if (storage.clearHistory(userId)) {
            chatContext().clear()
            true
        } else
            false
    }

    private suspend fun saveMessage(message: MessageData) {
        val context = chatContext()

        if (storage.insert(userId, message))
            context.add(message to instantNow())
    }

    private suspend fun contentLength(): Int =
        chatContext().sumOf { it.first.content.length }

    // keeps the history within the limit and never lets it start with an assistant message, an answer without its question only confuses the model
    private suspend fun ensureDialogLengthWithinLimit() {
        while (contentLength() > MAX_DIALOG_HISTORY_LENGTH ||
            chatContext().firstOrNull()?.first?.role == ChatRole.ASSISTANT
        ) {
            if (!removeOldestMessage()) break
        }
    }

    private suspend fun removeOldestMessage(): Boolean {
        val history = chatContext()
        if (history.isEmpty()) return false
        if (!storage.deleteOldestEntry(userId)) return false
        history.removeAt(0)
        return true
    }

    private suspend fun chatContext(): MutableList<Pair<MessageData, Instant>> {
        userChatContextMap[userId]?.let { return it }
        val history = storage.loadHistory(userId).toMutableList()
        return userChatContextMap.putIfAbsent(userId, history) ?: history
    }

    private fun userLock(): Mutex =
        userLocks.computeIfAbsent(userId) { Mutex() }

    private suspend fun <T> withUserLock(block: suspend () -> T): T =
        userLock().withLock { block() }
}
