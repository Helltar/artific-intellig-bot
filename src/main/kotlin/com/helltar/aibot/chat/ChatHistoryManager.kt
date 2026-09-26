package com.helltar.aibot.chat

import com.helltar.aibot.database.dao.chatHistoryDao
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Keeps the dialog history of a single user: user and assistant messages only.
 * The system prompt is not a part of it, it is built per request by [SystemPrompt].
 *
 * The history is limited in tokens, as the API counted them for the last request (see [fitTokenBudget]).
 * It grows until a request crosses [MAX_REQUEST_TOKENS] and is then cut to about a half at once:
 * between the cuts the beginning of the dialog stays the same, so the API keeps serving it from the
 * prompt cache. Dropping one old message per request would change that prefix on every request.
 */
class ChatHistoryManager(private val userId: Long, private val storage: ChatHistoryStorage = chatHistoryDao) {

    private companion object {
        const val MAX_REQUEST_TOKENS = 32_000
        const val TRIMMED_HISTORY_TOKENS = MAX_REQUEST_TOKENS / 2
        val log = KotlinLogging.logger {}
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
    }

    /**
     * Cuts the oldest messages once a request crossed the token budget.
     *
     * [requestTokens] is `usage.input_tokens` of the request and [requestChars] the length of all the text
     * it sent. Their ratio turns the budget into characters, so no local tokenizer is needed and the
     * estimate follows the language of the dialog.
     */
    suspend fun fitTokenBudget(requestTokens: Int, requestChars: Int): Unit = withUserLock {
        if (requestTokens <= MAX_REQUEST_TOKENS || requestChars <= 0) return@withUserLock

        val tokensPerChar = requestTokens.toDouble() / requestChars
        val sizeBefore = chatContext().size

        // never let the history start with an assistant message, an answer without its question only confuses the model
        while (contentLength() * tokensPerChar > TRIMMED_HISTORY_TOKENS ||
            chatContext().firstOrNull()?.first?.role == ChatRole.ASSISTANT
        ) {
            if (!removeOldestMessage()) break
        }

        val dropped = sizeBefore - chatContext().size
        log.info { "request took $requestTokens input tokens, $dropped oldest messages dropped from the history" }
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
