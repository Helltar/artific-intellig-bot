package com.helltar.aibot.commands.chat

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.commands.Commands

class ChatCtxRemove(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val repliedUser = message.replyToMessage?.from
        val repliedUserId: Long? = repliedUser?.id

        val (targetUserId, username) =
            if (isReply) {
                if (!isAdmin()) {
                    replyToMessage(Strings.ADMIN_ONLY_COMMAND)
                    return
                }

                repliedUserId?.let { it to " (<b>${repliedUser.firstName}</b>)" } ?: return
            } else
                this.userId to ""

        if (isCreator(targetUserId) && !isCreator(this.userId)) {
            replyToMessage(Strings.CREATOR_CONTEXT_CANNOT_BE_DELETED)
            return
        }

        if (ChatHistoryManager(targetUserId).clear())
            replyToMessage(Strings.CHAT_CONTEXT_REMOVED + username)
        else
            replyToMessage(Strings.CHAT_CONTEXT_EMPTY + username)
    }

    override fun commandName() =
        Commands.User.CMD_CHAT_CTX_REMOVE
}
