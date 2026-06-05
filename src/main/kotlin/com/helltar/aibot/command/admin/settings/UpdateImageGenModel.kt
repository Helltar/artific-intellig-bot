package com.helltar.aibot.command.admin.settings

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.configurationsDao

class UpdateImageGenModel(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            val imagesModel = configurationsDao.imageGenModel()
            replyToMessage(BotMessages.Usage.updateImageGenModel(imagesModel))
            return
        }

        val modelName = arguments[0].trim()

        if (modelName.length < 3) {
            replyToMessage(BotMessages.Models.BAD_MODEL_NAME_LENGTH)
            return
        }

        if (configurationsDao.updateImageGenModel(modelName))
            replyToMessage(BotMessages.Models.imagesSuccessUpdate(modelName))
        else
            replyToMessage(BotMessages.Models.IMAGES_FAIL_UPDATE)
    }

    override fun commandName() =
        CommandNames.Creator.CMD_UPDATE_IMAGE_GEN_MODEL
}
