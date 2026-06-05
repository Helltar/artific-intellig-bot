package com.helltar.aibot.commands.admin.allowlist

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.database.dao.chatAllowlistDao

class RemoveChat(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        val chatId = if (arguments.isNotEmpty()) arguments[0].toLongOrNull() else ctx.chatId()

        chatId?.let {
            if (chatAllowlistDao.remove(it))
                replyToMessage(BotMessages.Allowlist.CHAT_REMOVED)
            else
                replyToMessage(BotMessages.Allowlist.CHAT_NOT_EXISTS)
        }
    }

    override fun commandName() =
        CommandNames.Admin.CMD_RM_CHAT
}
