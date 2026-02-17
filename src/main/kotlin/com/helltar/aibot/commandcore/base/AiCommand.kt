package com.helltar.aibot.commandcore.base

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.database.dao.apiKeyDao
import com.helltar.aibot.database.dao.configurationsDao
import com.helltar.aibot.openai.ApiConfig

abstract class AiCommand(ctx: MessageContext) : BotCommand(ctx) {

    protected suspend fun chatModel() =
        configurationsDao.chatModel()

    protected suspend fun visionModel() =
        configurationsDao.visionModel()

    protected suspend fun imagesModel() =
        configurationsDao.imageGenModel()

    protected suspend fun openaiApiKey() =
        checkNotNull(apiKeyDao.getKey(ApiConfig.PROVIDER_NAME)) { "OpenAI API key is missing" }
}
