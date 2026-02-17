package com.helltar.aibot.commands.simple

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.commands.Commands

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
        Commands.Simple.CMD_START
}
