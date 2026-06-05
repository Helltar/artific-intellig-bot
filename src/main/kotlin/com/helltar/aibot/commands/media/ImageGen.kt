package com.helltar.aibot.commands.media

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.Strings
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.AiCommand
import com.helltar.aibot.openai.service.ImageGenService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.ActionType

class ImageGen(ctx: BotCommandContext) : AiCommand(ctx) {

    private companion object {
        val log = KotlinLogging.logger {}
    }

    override val chatAction = ActionType.UPLOAD_PHOTO

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
