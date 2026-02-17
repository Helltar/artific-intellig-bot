package com.helltar.aibot.commands.media

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Strings
import com.helltar.aibot.commandcore.CommandNames
import com.helltar.aibot.commandcore.base.AiCommand
import com.helltar.aibot.openai.service.ImageGenService
import io.github.oshai.kotlinlogging.KotlinLogging

class ImageGen(ctx: MessageContext) : AiCommand(ctx) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    override suspend fun run() {
        if (arguments.isEmpty()) {
            replyToMessage(Strings.Templates.IMG_GEN_COMMAND_USAGE_TEMPLATE_RAW.trimIndent())
            return
        }

        if (argumentsString.length > 3200) {
            replyToMessage(String.format(Strings.Command.MANY_CHARACTERS, 3200))
            return
        }

        try {
            val bytes = ImageGenService(imagesModel(), openaiApiKey()).generateImage(argumentsString)
            replyToMessageWithPhoto(bytes, argumentsString)
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(Strings.Chat.EXCEPTION)
        }
    }

    override fun commandName() =
        CommandNames.User.CMD_IMAGE_GEN
}
