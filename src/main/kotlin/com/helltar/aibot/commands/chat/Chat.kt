package com.helltar.aibot.commands.chat

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.Strings.localizedString
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.commands.AiCommand
import com.helltar.aibot.commands.chat.ChatHistoryManager.Companion.USER_MESSAGE_LIMIT
import com.helltar.aibot.exceptions.ImageTooLargeException
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.openai.service.ChatService
import com.helltar.aibot.openai.service.VisionService
import io.github.oshai.kotlinlogging.KotlinLogging

class Chat(ctx: MessageContext) : AiCommand(ctx) {

    private companion object {
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
                        ?: localizedString(Strings.VISION_DEFAULT_PROMPT, userLanguageCode)

                chatHistoryManager.saveUserMessage(message, prompt)

                retrieveVisionAnswer(prompt)
            }

        answer?.let {
            replyToMessage(it, messageIdToReply)
            chatHistoryManager.saveAssistantMessage(it)
        }
    }

    override fun commandName() =
        Commands.User.CMD_CHAT

    private suspend fun retrieveChatAnswer(messages: List<MessageData>): String? =
        try {
            ChatService(model = chatModel(), apiKey = openaiApiKey()).getReply(messages)
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(Strings.CHAT_EXCEPTION)
            null
        }

    private suspend fun retrieveVisionAnswer(prompt: String): String? {
        val photo =
            try {
                downloadPhoto() ?: return null
            } catch (_: ImageTooLargeException) {
                replyToMessage(Strings.IMAGE_MUST_BE_LESS_THAN.format("1.MB"))
                return null
            }

        return try {
            VisionService(model = visionModel(), apiKey = openaiApiKey()).analyzeImage(prompt, photo)
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(Strings.CHAT_EXCEPTION)
            null
        } finally {
            photo.delete()
        }
    }

    private fun replyToMessage(text: String, messageId: Int) {
        try {
            super.replyToMessage(text, messageId, webPagePreview = false)
        } catch (e: Exception) {
            log.error { e.message }
            replyWithTextDocument(text, Strings.TELEGRAM_API_EXCEPTION_RESPONSE_SAVED_TO_FILE)
        }
    }

    private suspend fun processUserMessage(): Int? {
        if (isNotReply && argumentsString.isBlank()) {
            replyToMessage(Strings.CHAT_HELLO)
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
                    replyToMessage(Strings.MESSAGE_TEXT_NOT_FOUND, messageId)
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
                replyToMessage(String.format(Strings.MANY_CHARACTERS, USER_MESSAGE_LIMIT))
                null
            }
        }
    }
}
