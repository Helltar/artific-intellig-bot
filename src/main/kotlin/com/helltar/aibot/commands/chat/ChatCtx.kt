package com.helltar.aibot.commands.chat

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.openai.ApiConfig.ChatRole
import com.helltar.aibot.openai.models.common.MessageData
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatCtx(ctx: MessageContext) : BotCommand(ctx) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    override suspend fun run() {
        val userId = getUserId() ?: return

        if (isCreator(userId) && !isCreator(this.userId)) {
            replyToMessage(Strings.CREATOR_CONTEXT_CANNOT_BE_VIEWED)
            return
        }

        val userChatHistory = ChatHistoryManager(userId).history()
        val text = formatUserChatHistory(userChatHistory)

        try {
            replyToMessage(text)
        } catch (e: Exception) {
            log.error { e.message }

            if (userChatHistory.isNotEmpty())
                replyWithTextDocument(text, Strings.TELEGRAM_API_EXCEPTION_CONTEXT_SAVED_TO_FILE)
        }
    }

    override fun commandName() =
        Commands.User.CMD_CHATCTX

    private suspend fun getUserId() =
        if (!isReply)
            this.userId
        else {
            if (isAdmin())
                message.replyToMessage.from.id
            else {
                replyToMessage(Strings.ADMIN_ONLY_COMMAND)
                null
            }
        }

    private fun formatUserChatHistory(userChatHistory: List<Pair<MessageData, Instant>>) =
        if (userChatHistory.isNotEmpty()) {
            val formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault())

            userChatHistory
                .filter { it.first.role == ChatRole.USER }
                .joinToString("\n") { """▫️ <b>${formatter.format(it.second)}</b> - ${it.first.content}""" }
        } else
            Strings.CHAT_CONTEXT_EMPTY
}
