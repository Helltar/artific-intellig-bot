package com.helltar.aibot.commands.general

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.BotCommand

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
        CommandNames.General.CMD_ABOUT
}
