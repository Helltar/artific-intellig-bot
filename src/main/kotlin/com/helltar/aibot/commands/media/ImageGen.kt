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
            replyToMessage(Strings.IMG_GEN_COMMAND_USAGE_TEMPLATE_RAW.trimIndent())
            return
        }

        if (argumentsString.length > 1000) {
            replyToMessage(String.format(Strings.MANY_CHARACTERS, 1000))
            return
        }

        try {
            val imageUrl = ImageGenService(model = imagesModel(), apiKey = openaiApiKey()).generateImage(argumentsString)
            log.debug { "image url: $imageUrl" }
            replyToMessageWithPhoto(imageUrl, argumentsString)
        } catch (e: Exception) {
            log.error { e.message }
            replyToMessage(Strings.CHAT_EXCEPTION)
        }
    }

    override fun commandName() =
        CommandNames.User.CMD_IMAGE_GEN
}
