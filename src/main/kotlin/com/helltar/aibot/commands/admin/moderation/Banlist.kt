package com.helltar.aibot.commands.admin.moderation

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.database.dao.banlistDao

class Banlist(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val list =
            banlistDao.list().joinToString("\n") {
                val username = it.username ?: it.firstName
                val reason = it.reason?.let { reason -> "<i>($reason)</i>" } ?: ""
                "<code>${it.userId}</code> <b>$username</b> $reason <i>(${it.bannedAt})</i>"
            }

        replyToMessage(list.ifEmpty { Strings.Ui.LIST_IS_EMPTY })
    }

    override fun commandName() =
        CommandNames.Admin.CMD_BAN_LIST
}
