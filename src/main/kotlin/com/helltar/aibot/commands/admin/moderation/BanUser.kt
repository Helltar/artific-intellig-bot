package com.helltar.aibot.commands.admin.moderation

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.Strings
import com.helltar.aibot.database.dao.banlistDao

class BanUser(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val user = ctx.message().replyToMessage?.from ?: return
        val reason = argumentsString.ifEmpty { null }

        if (banlistDao.ban(user, reason))
            replyToMessage(Strings.Moderation.USER_BANNED)
        else
            replyToMessage(Strings.Moderation.USER_ALREADY_BANNED)
    }

    override fun commandName() =
        CommandNames.Admin.CMD_BAN_USER
}
