package com.helltar.aibot.commands.admin.admins

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.database.dao.sudoersDao

class AddAdmin(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val userId = if (arguments.isNotEmpty()) arguments[0].toLongOrNull() else return

        userId?.let {
            val username = if (arguments.size >= 2) arguments[1] else null

            if (sudoersDao.add(it, username))
                replyToMessage(Strings.ADMIN_ADDED)
            else
                replyToMessage(Strings.ADMIN_EXISTS)
        }
    }

    override fun commandName() =
        Commands.Creator.CMD_ADD_ADMIN
}
