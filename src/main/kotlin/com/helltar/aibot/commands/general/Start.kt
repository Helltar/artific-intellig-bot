package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand

class Start(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage(
            """
            👋 Welcome to AI Bot!

            Here is how to start:
            • reply to any of my messages and write your question
            • or use <code>/${CommandNames.User.CMD_CHAT}</code> with your prompt
            • for images, use <code>/${CommandNames.User.CMD_IMAGE_GEN}</code> + prompt

            I'm ready when you are.
            """
                .trimIndent()
        )
    }

    override fun commandName() =
        CommandNames.General.CMD_START
}
