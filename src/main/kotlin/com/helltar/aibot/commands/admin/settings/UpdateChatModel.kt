package com.helltar.aibot.commands.admin.settings

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.configurationsDao

class UpdateChatModel(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            val chatModel = configurationsDao.chatModel()
            replyToMessage(BotMessages.Usage.updateChatModel(chatModel))
            return
        }

        val modelName = arguments[0].trim()

        if (modelName.length < 3) {
            replyToMessage(BotMessages.Models.BAD_MODEL_NAME_LENGTH)
            return
        }

        if (configurationsDao.updateChatModel(modelName) && configurationsDao.updateVisionModel(modelName))
            replyToMessage(BotMessages.Models.chatSuccessUpdate(modelName))
        else
            replyToMessage(BotMessages.Models.CHAT_FAIL_UPDATE)
    }

    override fun commandName() =
        CommandNames.Creator.CMD_UPDATE_CHAT_MODEL
}
