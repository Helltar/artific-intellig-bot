package com.helltar.aibot.commands.admin.ban

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.database.dao.banlistDao

class Banlist(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val list =
            banlistDao.list().joinToString("\n") {
                val username = it.username ?: it.firstName
                val reason = it.reason?.let { reason -> "<i>($reason)</i>" } ?: ""
                "<code>${it.userId}</code> <b>$username</b> $reason <i>(${it.bannedAt})</i>"
            }

        replyToMessage(list.ifEmpty { Strings.LIST_IS_EMPTY })
    }

    override fun commandName() =
        Commands.Admin.CMD_BAN_LIST
}
