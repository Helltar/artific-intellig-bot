package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.commandcore.CommandNames

class MyId(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage("<code>$userId</code>")
    }

    override fun commandName() =
        CommandNames.General.CMD_MYID
}
