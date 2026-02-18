package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames

class MyId(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage("<code>$userId</code>")
    }

    override fun commandName() =
        CommandNames.General.CMD_MYID
}
