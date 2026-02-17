package com.helltar.aibot.commands.admin.sudoers

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.commandcore.CommandNames
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
        CommandNames.Admin.CMD_RM_ADMIN
}
