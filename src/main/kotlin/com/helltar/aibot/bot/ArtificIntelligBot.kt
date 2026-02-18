package com.helltar.aibot.bot

import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.Runner
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.aibot.Config.BotConfig
import com.helltar.aibot.Config.botConfig
import com.helltar.aibot.command.CommandNames.toggleableCommands
import com.helltar.aibot.database.Database

class ArtificIntelligBot : BotModule {

    private val appConfig: BotConfig = botConfig

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val profile = args.firstOrNull() ?: ""
            Runner.run(profile, listOf(ArtificIntelligBot()))
        }
    }

    init {
        Database.init(appConfig, toggleableCommands)
    }

    override fun botHandler(unusedConfig: Config) =
        ArtificIntelligBotHandler(BotModuleOptions.createDefault(appConfig.telegramBotToken), appConfig)
}
