package com.helltar.aibot.commandcore.support

import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.exceptions.ImageTooLargeException
import com.helltar.aibot.utils.HtmlUtils.buildStyledHtmlPage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.util.concurrent.CompletableFuture

class CommandMessageSupport(
    private val ctx: MessageContext,
    private val message: Message,
    private val replyMessage: Message?
) {

    private companion object {
        const val TELEGRAM_MESSAGE_LENGTH_LIMIT = 4096
        val log = KotlinLogging.logger {}
    }

    fun replyToMessage(text: String, messageId: Int = message.messageId, webPagePreview: Boolean = false) {
        val messageIdToReply = if (replyMessage?.from?.isBot == false) messageId else message.messageId // todo: refact.

        if (text.length <= TELEGRAM_MESSAGE_LENGTH_LIMIT) {
            ctx.replyToMessage(text)
                .setReplyToMessageId(messageIdToReply)
                .setParseMode(ParseMode.HTML)
                .setWebPagePreviewEnabled(webPagePreview)
                .call(ctx.sender)
        } else
            chunkedReplyToMessage(text, messageIdToReply, webPagePreview)
    }

    fun replyToMessageWithDocument(fileId: String, caption: String): Int =
        ctx.replyWithDocument()
            .setFile(fileId)
            .setCaption(caption)
            .setReplyToMessageId(message.messageId)
            .call(ctx.sender)
            .messageId

    fun deleteMessage(messageId: Int): CompletableFuture<Boolean> =
        ctx.deleteMessage().setMessageId(messageId).callAsync(ctx.sender)

    fun sendDocument(file: File): Message =
        ctx.replyWithDocument()
            .setFile(file)
            .call(ctx.sender)

    fun replyToMessageWithPhoto(url: String, caption: String = "", messageId: Int? = message.messageId): Message =
        ctx.replyToMessageWithPhoto()
            .setFile(InputFile(url))
            .setCaption(caption)
            .setReplyToMessageId(messageId)
            .setParseMode(ParseMode.HTML)
            .call(ctx.sender)

    fun replyWithTextDocument(text: String, caption: String): Int {
        val title = "replyToMessageId-${message.messageId}"
        val html = buildStyledHtmlPage(title, text.trim()).byteInputStream()

        return ctx.replyWithDocument()
            .setFile("$title.html", html)
            .setCaption(caption)
            .setReplyToMessageId(message.messageId)
            .call(ctx.sender)
            .messageId
    }

    fun downloadPhoto(message: Message? = replyMessage, limitBytes: Int = 1024 * 1024): File? {
        val photo = message?.photo?.maxByOrNull { it.fileSize }

        return photo?.let {
            if (it.fileSize <= limitBytes) {
                try {
                    ctx.sender.downloadFile(Methods.getFile(it.fileId).call(ctx.sender))
                } catch (e: TelegramApiException) {
                    log.error { e.message }
                    null
                }
            } else
                throw ImageTooLargeException(limitBytes)
        }
    }

    private fun chunkedReplyToMessage(text: String, messageId: Int, webPagePreview: Boolean) {
        var messageIdToReply = messageId

        text.chunked(TELEGRAM_MESSAGE_LENGTH_LIMIT).forEach {
            if (it.isNotBlank()) {
                messageIdToReply =
                    ctx.replyToMessage(it)
                        .setReplyToMessageId(messageIdToReply)
                        .setParseMode(ParseMode.HTML)
                        .setWebPagePreviewEnabled(webPagePreview)
                        .call(ctx.sender)
                        .messageId
            }
        }
    }
}
