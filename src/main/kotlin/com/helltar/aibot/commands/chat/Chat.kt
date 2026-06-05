package com.helltar.aibot.commands.chat

import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.AiCommand
import com.helltar.aibot.exceptions.ImageTooLargeException
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.service.ChatService
import com.helltar.aibot.openai.service.VisionService
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class Chat(ctx: BotCommandContext) : AiCommand(ctx) {

    private companion object {
        const val USER_MESSAGE_LIMIT = 4000
        const val IMAGE_SIZE_LIMIT_BYTES = 1024 * 1024
        const val VISION_DEFAULT_PROMPT = "What's in this image?"
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z").withZone(ZoneId.systemDefault())
        val log = KotlinLogging.logger {}
    }

    private val chatHistoryManager = ChatHistoryManager(userId)

    override suspend fun run() {
        var messageIdToReply = message.messageId

        val answer =
            if (replyMessage?.hasPhoto() != true) {
                processUserMessage()?.let { messageId ->
                    messageIdToReply = messageId
                    retrieveChatAnswer(chatHistoryManager.messages())
                }
            } else {
                val prompt =
                    argumentsString.takeIf { it.isNotBlank() }
                        ?: VISION_DEFAULT_PROMPT

                chatHistoryManager.saveUserMessage(message, prompt)

                retrieveVisionAnswer(prompt)
            }

        answer?.let {
            replyToMessage(it, messageIdToReply)
            chatHistoryManager.saveAssistantMessage(it)
        }
    }

    override fun commandName() =
        CommandNames.User.CMD_CHAT

    private suspend fun retrieveChatAnswer(messages: List<MessageData>): String? =
        try {
            ChatService(chatModel(), openaiApiKey()).getReply(withCurrentTime(messages))
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(BotMessages.Chat.EXCEPTION)
            null
        }

    private fun withCurrentTime(messages: List<MessageData>): List<MessageData> {
        if (messages.isEmpty()) return messages
        val timeNote = MessageData(ChatRole.SYSTEM, "Current date and time: ${dateTimeFormatter.format(instantNow())}")
        return messages.dropLast(1) + timeNote + messages.last()
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
            val systemPrompt = chatHistoryManager.systemPrompt()
            VisionService(visionModel(), openaiApiKey()).analyzeImage(prompt, photo, systemPrompt)
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
        } catch (e: TelegramApiException) {
            log.error { e.message }
            replyWithTextDocument(text, BotMessages.Chat.savedToFile("response"))
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
                chatHistoryManager.saveUserMessage(message, text)
                messageId
            } else {
                replyToMessage(BotMessages.Command.manyCharacters(USER_MESSAGE_LIMIT))
                null
            }
        }
    }
}
