package com.helltar.aibot.command.base

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.database.dao.apiKeyDao
import com.helltar.aibot.database.dao.configurationsDao
import com.helltar.aibot.openai.ApiConfig

abstract class AiCommand(ctx: BotCommandContext) : BotCommand(ctx) {

    protected suspend fun chatModel() =
        configurationsDao.chatModel()

    protected suspend fun visionModel() =
        configurationsDao.visionModel()

    protected suspend fun imagesModel() =
        configurationsDao.imageGenModel()

    protected suspend fun openaiApiKey() =
        checkNotNull(apiKeyDao.getKey(ApiConfig.PROVIDER_NAME)) { "OpenAI API key is missing" }
}
