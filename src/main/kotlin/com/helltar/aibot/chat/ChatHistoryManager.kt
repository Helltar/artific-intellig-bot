package com.helltar.aibot.chat

import com.helltar.aibot.Config.SYSTEM_PROMPT_FILE
import com.helltar.aibot.database.dao.chatHistoryDao
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.DateTimeUtils.utcNow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class ChatHistoryManager(private val userId: Long) {

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

    suspend fun saveUserMessage(message: Message, messageText: String) = withUserLock {
        addSystemPromptIfNeeded(message)
        saveMessage(MessageData(ChatRole.USER, messageText))
        ensureDialogLengthWithinLimit()
    }

    suspend fun clear(): Boolean = withUserLock {
        if (chatHistoryDao.clearHistory(userId)) {
            chatContext().clear()
            true
        } else
            false
    }

    private suspend fun saveMessage(message: MessageData) {
        if (chatHistoryDao.insert(userId, message))
            chatContext().add(message to utcNow())
    }

    private suspend fun contentLength(): Int =
        chatContext().sumOf { it.first.content.length }

    private suspend fun removeSecondMessage() {
        if (chatHistoryDao.deleteOldestEntry(userId)) {
            val history = chatContext()
            if (history.size > 1) history.removeAt(1)
        }
    }

    private suspend fun ensureDialogLengthWithinLimit() {
        while (contentLength() > MAX_DIALOG_HISTORY_LENGTH)
            removeSecondMessage()
    }

    private suspend fun addSystemPromptIfNeeded(message: Message) {
        if (chatContext().isEmpty()) {
            val systemPrompt = File(SYSTEM_PROMPT_FILE).readText()
            val username = message.from.userName ?: message.from.firstName
            val chatTitle = message.chat.title ?: username
            val systemPromptContent = systemPrompt.format(chatTitle, username, userId)
            val systemPromptData = MessageData(ChatRole.SYSTEM, systemPromptContent)
            saveMessage(systemPromptData)
        }
    }

    private suspend fun chatContext(): MutableList<Pair<MessageData, Instant>> {
        userChatContextMap[userId]?.let { return it }
        val history = chatHistoryDao.loadHistory(userId).toMutableList()
        return userChatContextMap.putIfAbsent(userId, history) ?: history
    }

    private fun userLock(): Mutex =
        userLocks.computeIfAbsent(userId) { Mutex() }

    private suspend fun <T> withUserLock(block: suspend () -> T): T =
        userLock().withLock { block() }
}
