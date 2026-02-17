package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.commands.Commands

class About(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        replyToMessage(
            """
            <a href="https://github.com/Helltar/artific-intellig-bot">AᎥ</a>
            Contact: https://helltar.com
            Source Code:
            """
                .trimIndent(), webPagePreview = true
        )
    }

    override fun commandName() =
        Commands.Simple.CMD_ABOUT
}
