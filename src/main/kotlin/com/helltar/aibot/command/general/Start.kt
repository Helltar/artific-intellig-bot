package com.helltar.aibot.command.general

import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand

class Start(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage(BotMessages.General.start())
    }

    override fun commandName() =
        CommandNames.General.CMD_START
}
