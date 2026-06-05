package com.helltar.aibot.commands.chat

import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import com.helltar.aibot.utils.StringUtils.singleLineTruncated
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.util.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatCtx(ctx: BotCommandContext) : BotCommand(ctx) {

    private companion object {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault())
        val log = KotlinLogging.logger {}
    }

    override suspend fun run() {
        val userId = getUserId() ?: return

        if (isCreator(userId) && !isCreator(this.userId)) {
            replyToMessage(BotMessages.Command.CREATOR_CONTEXT_CANNOT_BE_VIEWED)
            return
        }

        val userChatHistory = ChatHistoryManager(userId).history()
        val text = formatUserChatHistory(userChatHistory)

        try {
            replyToMessage(text)
        } catch (e: Exception) {
            log.error { e.message }

            if (userChatHistory.isNotEmpty())
                replyWithTextDocument(text, BotMessages.Chat.savedToFile("context"))
        }
    }

    override fun commandName() =
        CommandNames.User.CMD_CHATCTX

    private suspend fun getUserId() =
        if (!isReply)
            this.userId
        else {
            if (isAdmin())
                message.replyToMessage.from.id
            else {
                replyToMessage(BotMessages.Command.ADMIN_ONLY)
                null
            }
        }

    private fun formatUserChatHistory(userChatHistory: List<Pair<MessageData, Instant>>): String {
        val userMessages = userChatHistory.filter { it.first.role == ChatRole.USER }

        if (userMessages.isEmpty())
            return BotMessages.Chat.CONTEXT_EMPTY

        return userMessages.joinToString("\n\n") { (message, time) ->
            "▫️ <b>${dateFormatter.format(time)}</b>\n${message.content.singleLineTruncated(100).escapeHTML()}"
        }
    }
}
