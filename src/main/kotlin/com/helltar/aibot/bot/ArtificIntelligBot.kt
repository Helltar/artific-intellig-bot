package com.helltar.aibot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModule
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.beans.Config
import com.helltar.aibot.Config.BotConfig
import com.helltar.aibot.health.Heartbeat
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator

class ArtificIntelligBot(private val botConfig: BotConfig, private val heartbeat: Heartbeat) : BotModule {

    override fun botHandler(unusedConfig: Config): BotHandler {
        val getUpdates = DefaultGetUpdatesGenerator()

        // the generator is where the heartbeat hooks in, because the session calls it once per poll
        // cycle before every request. the update consumer would not do: the session skips it entirely
        // on an empty batch, so a bot nobody writes to would look dead within minutes.
        val options =
            BotModuleOptions.create(botConfig.telegramBotToken)
                .getUpdatesGenerator { offset ->
                    heartbeat.markPoll()
                    getUpdates.apply(offset)
                }
                .build()

        return ArtificIntelligBotHandler(options, botConfig)
    }
}
