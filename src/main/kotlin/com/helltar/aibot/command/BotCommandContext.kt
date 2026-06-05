package com.helltar.aibot.command

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Config.BotConfig

data class BotCommandContext(
    val messageContext: MessageContext,
    val botConfig: BotConfig
)
