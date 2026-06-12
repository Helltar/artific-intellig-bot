package com.helltar.aibot.command.base

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.database.dao.configurationsDao

abstract class AiCommand(ctx: BotCommandContext) : BotCommand(ctx) {

    private val botConfig = ctx.botConfig

    protected suspend fun chatModel() =
        configurationsDao.chatModel()

    protected suspend fun visionModel() =
        configurationsDao.visionModel()

    protected suspend fun imagesModel() =
        configurationsDao.imageGenModel()

    protected fun openaiApiKey() =
        botConfig.openaiApiKey
}
