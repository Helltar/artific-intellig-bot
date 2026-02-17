package com.helltar.aibot.commands.admin.admins

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.database.dao.sudoersDao

class RemoveAdmin(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val userId = if (arguments.isNotEmpty()) arguments[0].toLongOrNull() else return

        userId?.let {
            if (isCreator(it)) return

            if (sudoersDao.remove(it))
                replyToMessage(Strings.ADMIN_REMOVED)
            else
                replyToMessage(Strings.ADMIN_NOT_EXISTS)
        }
    }

    override fun commandName() =
        Commands.Admin.CMD_RM_ADMIN
}
