package com.helltar.aibot.commands.admin.config

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.database.dao.configurationsDao

class UpdateChatModel(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            val chatModel = configurationsDao.chatModel()
            replyToMessage(Strings.UPDATE_CHAT_MODEL_COMMAND_USAGE_TEMPLATE_RAW.trimIndent().format(chatModel))
            return
        }

        val modelName = arguments[0].trim()

        if (modelName.length < 3) {
            replyToMessage(Strings.BAD_MODEL_NAME_LENGTH)
            return
        }

        if (configurationsDao.updateChatModel(modelName) && configurationsDao.updateVisionModel(modelName))
            replyToMessage(Strings.CHAT_MODEL_SUCCESS_UPDATE.format(modelName))
        else
            replyToMessage(Strings.CHAT_MODEL_FAIL_UPDATE)
    }

    override fun commandName() =
        Commands.Creator.CMD_UPDATE_CHAT_MODEL
}
