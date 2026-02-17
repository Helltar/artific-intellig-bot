package com.helltar.aibot.commands.admin.ban

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.Strings
import com.helltar.aibot.database.dao.banlistDao

class BanUser(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val user = ctx.message().replyToMessage?.from ?: return
        val reason = argumentsString.ifEmpty { null }

        if (banlistDao.ban(user, reason))
            replyToMessage(Strings.USER_BANNED)
        else
            replyToMessage(Strings.USER_ALREADY_BANNED)
    }

    override fun commandName() =
        Commands.Admin.CMD_BAN_USER
}
