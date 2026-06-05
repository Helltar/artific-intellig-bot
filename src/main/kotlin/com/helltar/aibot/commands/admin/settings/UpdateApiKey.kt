package com.helltar.aibot.commands.admin.settings

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.apiKeyDao
import com.helltar.aibot.openai.ApiConfig

class UpdateApiKey(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            replyToMessage(BotMessages.Usage.updateApiKey())
            return
        }

        val provider = ApiConfig.PROVIDER_NAME
        val apiKey = arguments[0].trim()

        if (apiKey.length < 16) {
            replyToMessage(BotMessages.ApiKey.BAD_LENGTH)
            return
        }

        val currentApiKey = apiKeyDao.getKey(provider)

        if (currentApiKey == apiKey) {
            replyToMessage(BotMessages.ApiKey.successUpdate(provider))
            return
        }

        if (currentApiKey == null) {
            if (apiKeyDao.add(provider, apiKey))
                replyToMessage(BotMessages.ApiKey.successAdd(provider))
            else
                replyToMessage(BotMessages.ApiKey.failAdd(provider))
        } else {
            if (apiKeyDao.update(provider, apiKey))
                replyToMessage(BotMessages.ApiKey.successUpdate(provider))
            else
                replyToMessage(BotMessages.ApiKey.failUpdate(provider))
        }
    }

    override fun commandName() =
        CommandNames.Creator.CMD_UPDATE_API_KEY
}
