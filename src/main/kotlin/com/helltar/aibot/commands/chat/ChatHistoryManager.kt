package com.helltar.aibot.commands.chat

import com.helltar.aibot.Config.SYSTEM_PROMPT_FILE
import com.helltar.aibot.database.dao.chatHistoryDao
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.DateTimeUtils.utcNow
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.io.File
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class ChatHistoryManager(private val userId: Long) {

    companion object {
        const val USER_MESSAGE_LIMIT = 4000
        private const val DIALOG_HISTORY_LIMIT = USER_MESSAGE_LIMIT * 6
        private val userChatContextMap = ConcurrentHashMap<Long, CopyOnWriteArrayList<Pair<MessageData, Instant>>>()
    }

    suspend fun history(): List<Pair<MessageData, Instant>> =
        chatHistory()

    suspend fun messages(): List<MessageData> =
        history().map { it.first }

    suspend fun saveAssistantMessage(message: String): Unit =
        saveMessage(MessageData(ChatRole.ASSISTANT, message))

    suspend fun saveUserMessage(message: Message, messageText: String) {
        addSystemPromptIfNeeded(message)
        saveMessage(MessageData(ChatRole.USER, messageText))
        ensureDialogLengthWithinLimit()
    }

    suspend fun clear(): Boolean =
        if (chatHistoryDao.clearHistory(userId)) {
            chatHistory().clear()
            true
        } else
            false

    private suspend fun saveMessage(message: MessageData) {
        if (chatHistoryDao.insert(userId, message))
            chatHistory().add(message to utcNow())
    }

    private suspend fun contentLength(): Int =
        messages().sumOf { it.content.length }

    private suspend fun removeSecondMessage() {
        if (chatHistoryDao.deleteOldestEntry(userId)) {
            val history = chatHistory()
            if (history.size > 1) history.removeAt(1)
        }
    }

    private suspend fun ensureDialogLengthWithinLimit() {
        while (contentLength() > DIALOG_HISTORY_LIMIT)
            removeSecondMessage()
    }

    private suspend fun addSystemPromptIfNeeded(message: Message) {
        if (chatHistory().isEmpty()) {
            val systemPrompt = File(SYSTEM_PROMPT_FILE).readText()
            val username = message.from.userName ?: message.from.firstName
            val chatTitle = message.chat.title ?: username
            val systemPromptContent = systemPrompt.format(chatTitle, username, userId)
            val systemPromptData = MessageData(ChatRole.SYSTEM, systemPromptContent)
            saveMessage(systemPromptData)
        }
    }

    private suspend fun chatHistory() =
        userChatContextMap
            .getOrPut(userId) { CopyOnWriteArrayList(chatHistoryDao.loadHistory(userId)) }
}
