package com.helltar.aibot.commands.admin.moderation

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.banlistDao

class Banlist(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        val list =
            banlistDao.list().joinToString("\n") {
                val username = it.username ?: it.firstName
                val reason = it.reason?.let { reason -> "<i>($reason)</i>" } ?: ""
                "<code>${it.userId}</code> <b>$username</b> $reason <i>(${it.bannedAt})</i>"
            }

        replyToMessage(list.ifEmpty { BotMessages.Ui.LIST_IS_EMPTY })
    }

    override fun commandName() =
        CommandNames.Admin.CMD_BAN_LIST
}
