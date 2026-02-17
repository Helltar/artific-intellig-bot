package com.helltar.aibot.commands.admin.settings

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.database.dao.configurationsDao

class UpdateImageGenModel(ctx: MessageContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            val imagesModel = configurationsDao.imageGenModel()
            replyToMessage(Strings.UPDATE_IMAGES_MODEL_COMMAND_USAGE_TEMPLATE_RAW.trimIndent().format(imagesModel))
            return
        }

        val modelName = arguments[0].trim()

        if (modelName.length < 3) {
            replyToMessage(Strings.BAD_MODEL_NAME_LENGTH)
            return
        }

        if (configurationsDao.updateImageGenModel(modelName))
            replyToMessage(Strings.IMAGES_MODEL_SUCCESS_UPDATE.format(modelName))
        else
            replyToMessage(Strings.IMAGES_MODEL_FAIL_UPDATE)
    }

    override fun commandName() =
        CommandNames.Creator.CMD_UPDATE_IMAGE_GEN_MODEL
}
