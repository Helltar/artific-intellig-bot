package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.commandcore.CommandNames

class Start(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage(
            """
            Welcome to the AI Bot! 🤖✨
 
            To start a conversation, please reply to this message.
        
            How can I assist you today?
            """
                .trimIndent()
        )
    }

    override fun commandName() =
        CommandNames.General.CMD_START
}
