package com.helltar.aibot.command.chat

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames

class ChatCtxRemove(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        val repliedUser = message.replyToMessage?.from
        val repliedUserId: Long? = repliedUser?.id

        val (targetUserId, username) =
            if (isReply) {
                if (!isAdmin()) {
                    replyToMessage(BotMessages.Command.ADMIN_ONLY)
                    return
                }

                repliedUserId?.let { it to " (<b>${repliedUser.firstName}</b>)" } ?: return
            } else
                this.userId to ""

        if (isCreator(targetUserId) && !isCreator(this.userId)) {
            replyToMessage(BotMessages.Command.CREATOR_CONTEXT_CANNOT_BE_DELETED)
            return
        }

        if (ChatHistoryManager(targetUserId).clear())
            replyToMessage(BotMessages.Chat.contextRemoved(username))
        else
            replyToMessage(BotMessages.Chat.contextEmpty(username))
    }

    override fun commandName() =
        CommandNames.User.CMD_CHAT_CTX_REMOVE
}
