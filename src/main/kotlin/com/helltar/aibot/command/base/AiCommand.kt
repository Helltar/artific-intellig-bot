package com.helltar.aibot.command.base

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.database.dao.configurationsDao
import com.helltar.aibot.openai.ApiConfig.ReasoningEffort

abstract class AiCommand(ctx: BotCommandContext) : BotCommand(ctx) {

    private val botConfig = ctx.botConfig

    protected suspend fun chatModel() =
        configurationsDao.chatModel()

    protected suspend fun visionModel() =
        configurationsDao.visionModel()

    protected suspend fun imagesModel() =
        configurationsDao.imageGenModel()

    // null leaves the reasoning effort to the model
    protected suspend fun reasoningEffort(): String? =
        configurationsDao.reasoningEffort().takeUnless { it == ReasoningEffort.MODEL_DEFAULT }

    protected fun openaiApiKey() =
        botConfig.openaiApiKey
}
