package com.helltar.aibot.commands.admin.allowlist

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.chatAllowlistDao

class ChatAllowlist(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val text =
            chatAllowlistDao.list().joinToString("\n") {
                val title = it.title?.let { title -> "<i>($title)</i>" } ?: "null"
                "<code>${it.chatId}</code> $title <i>(${it.createdAt})</i>"
            }

        replyToMessage(text.ifEmpty { Strings.Ui.LIST_IS_EMPTY })
    }

    override fun commandName() =
        CommandNames.Admin.CMD_CHAT_ALLOW_LIST
}
