package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.commands.Commands

class MyId(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage("<code>$userId</code>")
    }

    override fun commandName() =
        Commands.General.CMD_MYID
}
