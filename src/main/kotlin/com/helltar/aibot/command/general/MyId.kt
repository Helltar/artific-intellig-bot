package com.helltar.aibot.command.general

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.command.CommandNames

class MyId(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage("<code>$userId</code>")
    }

    override fun commandName() =
        CommandNames.General.CMD_MYID
}
