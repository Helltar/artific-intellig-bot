package com.helltar.aibot.commands.chat

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.chat.ChatHistoryManager
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames

class ChatCtxRemove(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val repliedUser = message.replyToMessage?.from
        val repliedUserId: Long? = repliedUser?.id

        val (targetUserId, username) =
            if (isReply) {
                if (!isAdmin()) {
                    replyToMessage(Strings.Command.ADMIN_ONLY)
                    return
                }

                repliedUserId?.let { it to " (<b>${repliedUser.firstName}</b>)" } ?: return
            } else
                this.userId to ""

        if (isCreator(targetUserId) && !isCreator(this.userId)) {
            replyToMessage(Strings.Command.CREATOR_CONTEXT_CANNOT_BE_DELETED)
            return
        }

        if (ChatHistoryManager(targetUserId).clear())
            replyToMessage(Strings.Chat.CONTEXT_REMOVED + username)
        else
            replyToMessage(Strings.Chat.CONTEXT_EMPTY + username)
    }

    override fun commandName() =
        CommandNames.User.CMD_CHAT_CTX_REMOVE
}
