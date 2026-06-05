package com.helltar.aibot.commands.general

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand

class About(ctx: BotCommandContext) : BotCommand(ctx) {

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
