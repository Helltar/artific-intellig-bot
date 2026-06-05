package com.helltar.aibot.command.admin.allowlist

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.chatAllowlistDao

class ChatAllowlist(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        val text =
            chatAllowlistDao.list().joinToString("\n") {
                val title = it.title?.let { title -> "<i>($title)</i>" } ?: "null"
                "<code>${it.chatId}</code> $title <i>(${it.createdAt})</i>"
            }

        replyToMessage(text.ifEmpty { BotMessages.Ui.LIST_IS_EMPTY })
    }

    override fun commandName() =
        CommandNames.Admin.CMD_CHAT_ALLOW_LIST
}
