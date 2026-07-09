package com.helltar.aibot.chat

import com.helltar.aibot.Config.SYSTEM_PROMPT_FILE
import com.helltar.aibot.database.dao.chatHistoryDao
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class ChatHistoryManager(private val userId: Long, private val storage: ChatHistoryStorage = chatHistoryDao) {

    private companion object {
        const val MAX_DIALOG_HISTORY_LENGTH = 24576 // todo: tokens
        val placeholderRegex = Regex("""\{(room_name|user_name|user_id)}""")
        val systemPromptTemplate by lazy { File(SYSTEM_PROMPT_FILE).readText() }
        val userChatContextMap = ConcurrentHashMap<Long, MutableList<Pair<MessageData, Instant>>>()
        val userLocks = ConcurrentHashMap<Long, Mutex>()
    }

    suspend fun history(): List<Pair<MessageData, Instant>> = withUserLock {
        chatContext().toList()
    }

    suspend fun messages(): List<MessageData> = withUserLock {
        chatContext().map { it.first }
    }

    suspend fun systemPrompt(): String? = withUserLock {
        chatContext().firstOrNull()?.first?.takeIf { it.role == ChatRole.SYSTEM }?.content
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
        if (storage.clearHistory(userId)) {
            chatContext().clear()
            true
        } else
            false
    }

    private suspend fun saveMessage(message: MessageData) {
        if (storage.insert(userId, message))
            chatContext().add(message to instantNow())
    }

    private suspend fun contentLength(): Int =
        chatContext().sumOf { it.first.content.length }

    private suspend fun removeSecondMessage(): Boolean {
        val history = chatContext()
        if (history.size <= 1) return false
        if (!storage.deleteOldestEntry(userId)) return false
        history.removeAt(1)
        return true
    }

    private suspend fun ensureDialogLengthWithinLimit() {
        while (contentLength() > MAX_DIALOG_HISTORY_LENGTH ||
            chatContext().getOrNull(1)?.first?.role == ChatRole.ASSISTANT
        ) {
            if (!removeSecondMessage()) break
        }
    }

    private suspend fun addSystemPromptIfNeeded(message: Message) {
        val context = chatContext()

        if (context.firstOrNull()?.first?.role == ChatRole.SYSTEM)
            return

        val username = message.from.userName ?: message.from.firstName
        val chatTitle = message.chat.title ?: username
        val replacements = mapOf("room_name" to chatTitle, "user_name" to username, "user_id" to userId.toString())
        val systemPromptContent = placeholderRegex.replace(systemPromptTemplate) { replacements.getValue(it.groupValues[1]) }
        val systemPromptData = MessageData(ChatRole.SYSTEM, systemPromptContent)

        context.add(0, systemPromptData to instantNow())
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
