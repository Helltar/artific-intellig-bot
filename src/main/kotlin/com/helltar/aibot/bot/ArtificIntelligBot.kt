package com.helltar.aibot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.aibot.Config.BotConfig

class ArtificIntelligBot(private val botConfig: BotConfig) : BotModule {

    override fun botHandler(unusedConfig: Config): BotHandler =
        ArtificIntelligBotHandler(BotModuleOptions.createDefault(botConfig.telegramBotToken), botConfig)
}
