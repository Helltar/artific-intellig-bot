package com.helltar.aibot.commands.admin.sudoers

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.database.dao.sudoersDao
import com.helltar.aibot.database.models.SudoersData

class AdminList(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        val list = getFormattedSudoersList(sudoersDao.list())
        replyToMessage(list.ifEmpty { Strings.Ui.LIST_IS_EMPTY })
    }

    override fun commandName() =
        CommandNames.Admin.CMD_ADMIN_LIST

    private fun getFormattedSudoersList(sudoers: List<SudoersData>) =
        sudoers.joinToString("\n") { sudoer -> formatSudoer(sudoer) }

    private fun formatSudoer(sudoer: SudoersData) =
        "<code>${sudoer.userId}</code> <b>${sudoer.username}</b> <i>(${sudoer.createdAt})</i>"
}
