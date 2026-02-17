package com.helltar.aibot.commands.admin.moderation

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.Strings
import com.helltar.aibot.database.dao.banlistDao

class UnbanUser(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val userId = if (arguments.isNotEmpty()) arguments[0].toLongOrNull() else ctx.message().replyToMessage?.from?.id

        userId?.let {
            if (banlistDao.unban(it))
                replyToMessage(Strings.USER_UNBANNED)
            else
                replyToMessage(Strings.USER_NOT_BANNED)
        }
    }

    override fun commandName() =
        CommandNames.Admin.CMD_UNBAN_USER
}
