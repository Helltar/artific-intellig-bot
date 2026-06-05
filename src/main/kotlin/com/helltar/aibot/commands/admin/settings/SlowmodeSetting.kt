package com.helltar.aibot.commands.admin.settings

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.configurationsDao

class SlowmodeSetting(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty() || arguments[0].toIntOrNull() == null) {
            val maxUsageCount = configurationsDao.slowmodeMaxUsageCount()
            replyToMessage(BotMessages.Usage.slowmode(maxUsageCount))
            return
        }

        arguments[0].toIntOrNull()?.let { newMax ->
            if (configurationsDao.updateSlowmodeMaxUsageCount(newMax))
                replyToMessage(BotMessages.Slowmode.updated(newMax))
            else
                replyToMessage(BotMessages.Slowmode.CHANGE_FAIL)
        }
    }

    override fun commandName() =
        CommandNames.Creator.CMD_SLOWMODE
}
