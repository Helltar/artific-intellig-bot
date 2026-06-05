package com.helltar.aibot.command.admin.sudoers

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.database.dao.sudoersDao

class RemoveAdmin(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        val userId = if (arguments.isNotEmpty()) arguments[0].toLongOrNull() else return

        userId?.let {
            if (isCreator(it)) return

            if (sudoersDao.remove(it))
                replyToMessage(BotMessages.Admins.REMOVED)
            else
                replyToMessage(BotMessages.Admins.NOT_EXISTS)
        }
    }

    override fun commandName() =
        CommandNames.Admin.CMD_RM_ADMIN
}
