package com.helltar.aibot.commands.admin.settings

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.database.dao.configurationsDao

class SlowmodeSetting(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty() || arguments[0].toIntOrNull() == null) {
            val maxUsageCount = configurationsDao.slowmodeMaxUsageCount()
            replyToMessage(Strings.SLOWMODE_COMMAND_USAGE_TEMPLATE_RAW.trimIndent().format(maxUsageCount))
            return
        }

        arguments[0].toIntOrNull()?.let { newMax ->
            if (configurationsDao.updateSlowmodeMaxUsageCount(newMax))
                replyToMessage(Strings.SLOWMODE_SUCCESSFULLY_CHANGED.format(newMax))
            else
                replyToMessage(Strings.SLOWMODE_CHANGE_FAIL)
        }
    }

    override fun commandName() =
        CommandNames.Creator.CMD_SLOWMODE
}
