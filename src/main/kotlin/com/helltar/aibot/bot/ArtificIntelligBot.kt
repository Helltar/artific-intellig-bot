package com.helltar.aibot.bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.aibot.Config.botConfig
import com.helltar.aibot.commandcore.CommandNames.toggleableCommands
import com.helltar.aibot.database.Database

class ArtificIntelligBot : BotModule {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Runner.run("", listOf(ArtificIntelligBot()))
        }
    }

    init {
        Database.init(botConfig, toggleableCommands)
    }

    override fun botHandler(config: Config) =
        ArtificIntelligBotHandler(BotModuleOptions.createDefault(botConfig.telegramBotToken))
}
