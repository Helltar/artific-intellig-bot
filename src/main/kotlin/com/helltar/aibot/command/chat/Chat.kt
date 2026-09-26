package com.helltar.aibot.command.chat

import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.chat.SystemPrompt
import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.AiCommand
import com.helltar.aibot.exceptions.ImageTooLargeException
import com.helltar.aibot.exceptions.TelegramFormattingException
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.service.ChatReply
import com.helltar.aibot.openai.service.ChatService
import com.helltar.aibot.openai.service.VisionService
import io.github.oshai.kotlinlogging.KotlinLogging

class Chat(ctx: BotCommandContext) : AiCommand(ctx) {

    private companion object {
        const val USER_MESSAGE_LIMIT = 4000
        const val IMAGE_SIZE_LIMIT_BYTES = 1024 * 1024
        const val VISION_DEFAULT_PROMPT = "What's in this image?"
        val log = KotlinLogging.logger {}
    }

    private val chatHistoryManager = ChatHistoryManager(userId)

    override suspend fun run() {
        if (replyMessage?.hasPhoto() == true)
            answerAboutPhoto()
        else
            answerInChat()
    }

    override fun commandName() =
        CommandNames.User.CMD_CHAT

    private suspend fun answerInChat() {
        val messageId = processUserMessage() ?: return

        // the context goes after the history: everything before it stays the same between requests and can be reused by the api as a cached prompt prefix
        val input = chatHistoryManager.messages() + MessageData(ChatRole.SYSTEM, chatContext())
        val reply = retrieveChatReply(input) ?: return

        sendAnswer(reply.text, messageId)

        // cut only after the answer is out, the user does not wait for the database
        reply.usage?.let { usage ->
            val requestChars = SystemPrompt.instructions.length + input.sumOf { it.content.length }
            chatHistoryManager.fitTokenBudget(usage.inputTokens, requestChars)
        }
    }

    private suspend fun answerAboutPhoto() {
        val prompt =
            argumentsString.takeIf { it.isNotBlank() }
                ?: VISION_DEFAULT_PROMPT

        chatHistoryManager.saveUserMessage(prompt)

        retrieveVisionAnswer(prompt)?.let { sendAnswer(it, message.messageId) }
    }

    private suspend fun sendAnswer(text: String, messageId: Int) {
        replyToMessage(text, messageId)
        chatHistoryManager.saveAssistantMessage(text)
    }

    private suspend fun retrieveChatReply(input: List<MessageData>): ChatReply? =
        try {
            ChatService(chatModel(), openaiApiKey(), userId, reasoningEffort()).getReply(input, SystemPrompt.instructions)
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(BotMessages.Chat.EXCEPTION)
            null
        }

    private fun chatContext(): String {
        val userName = message.from.userName ?: message.from.firstName
        return SystemPrompt.context(roomName = message.chat.title ?: userName, userName, userId)
    }

    private suspend fun retrieveVisionAnswer(prompt: String): String? {
        val photo =
            try {
                downloadPhoto(limitBytes = IMAGE_SIZE_LIMIT_BYTES) ?: return null
            } catch (_: ImageTooLargeException) {
                replyToMessage(BotMessages.Chat.imageMustBeLessThan(IMAGE_SIZE_LIMIT_BYTES))
                return null
            }

        return try {
            VisionService(visionModel(), openaiApiKey(), userId, reasoningEffort())
                .analyzeImage(prompt, photo, SystemPrompt.instructions, chatContext())
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(BotMessages.Chat.EXCEPTION)
            null
        } finally {
            photo.delete()
        }
    }

    private fun replyToMessage(text: String, messageId: Int) {
        try {
            super.replyToMessage(text, messageId, webPagePreview = false)
        } catch (e: TelegramFormattingException) {
            log.error { e.message }
            replyWithHtmlDocument(text, BotMessages.Chat.savedToFile("response"))
        }
    }

    private suspend fun processUserMessage(): Int? {
        if (isNotReply && argumentsString.isBlank()) {
            replyToMessage(BotMessages.Chat.HELLO)
            return null
        }

        var text: String? = argumentsString
        var messageId = message.messageId

        if (isReply) {
            val message = replyMessage!!

            if (isNotMyMessage(message)) {
                text = message.text ?: message.caption
                messageId = message.messageId

                if (text.isNullOrBlank()) {
                    replyToMessage(BotMessages.Chat.MESSAGE_TEXT_NOT_FOUND, messageId)
                    return null
                }

                if (argumentsString.isNotBlank()) {
                    text = "$argumentsString: '$text'"
                    messageId = this.message.messageId
                }
            } else
                text = this.message.text
        }

        return text?.let {
            text = it.trim()

            if (text.length <= USER_MESSAGE_LIMIT) {
                chatHistoryManager.saveUserMessage(text)
                messageId
            } else {
                replyToMessage(BotMessages.Command.manyCharacters(USER_MESSAGE_LIMIT))
                null
            }
        }
    }
}
