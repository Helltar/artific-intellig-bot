package com.helltar.aibot.commands.admin.moderation

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.database.dao.banlistDao

class UnbanUser(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        val userId = if (arguments.isNotEmpty()) arguments[0].toLongOrNull() else ctx.message().replyToMessage?.from?.id

        userId?.let {
            if (banlistDao.unban(it))
                replyToMessage(BotMessages.Moderation.USER_UNBANNED)
            else
                replyToMessage(BotMessages.Moderation.USER_NOT_BANNED)
        }
    }

    override fun commandName() =
        CommandNames.Admin.CMD_UNBAN_USER
}
