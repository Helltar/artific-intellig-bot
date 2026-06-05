package com.helltar.aibot

import com.annimon.tgbotsmodule.Runner
import com.helltar.aibot.bot.ArtificIntelligBot
import com.helltar.aibot.command.CommandNames.toggleableCommands
import com.helltar.aibot.database.Database

fun main(args: Array<String>) {
    val botConfig = Config.botConfig
    Database.init(botConfig, toggleableCommands)
    Runner.run(args.firstOrNull().orEmpty(), listOf(ArtificIntelligBot(botConfig)))
}
