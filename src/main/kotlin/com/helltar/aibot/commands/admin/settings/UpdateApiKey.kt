package com.helltar.aibot.commands.admin.settings

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.apiKeyDao
import com.helltar.aibot.openai.ApiConfig

class UpdateApiKey(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            replyToMessage(Strings.Templates.UPDATE_API_KEY_COMMAND_USAGE_TEMPLATE_RAW.trimIndent())
            return
        }

        val provider = ApiConfig.PROVIDER_NAME
        val apiKey = arguments[0].trim()

        if (apiKey.length < 16) {
            replyToMessage(Strings.ApiKey.BAD_LENGTH)
            return
        }

        val currentApiKey = apiKeyDao.getKey(provider)

        if (currentApiKey == apiKey) {
            replyToMessage(Strings.ApiKey.SUCCESS_UPDATE.format(provider))
            return
        }

        if (currentApiKey == null) {
            if (apiKeyDao.add(provider, apiKey))
                replyToMessage(Strings.ApiKey.SUCCESS_ADD.format(provider))
            else
                replyToMessage(Strings.ApiKey.FAIL_ADD.format(provider))
        } else {
            if (apiKeyDao.update(provider, apiKey))
                replyToMessage(Strings.ApiKey.SUCCESS_UPDATE.format(provider))
            else
                replyToMessage(Strings.ApiKey.FAIL_UPDATE.format(provider))
        }
    }

    override fun commandName() =
        CommandNames.Creator.CMD_UPDATE_API_KEY
}
