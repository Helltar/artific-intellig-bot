package com.helltar.aibot.commands

import com.annimon.tgbotsmodule.api.methods.Methods
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Config.creatorId
import com.helltar.aibot.Config.telegramBotUsername
import com.helltar.aibot.database.dao.banlistDao
import com.helltar.aibot.database.dao.chatAllowlistDao
import com.helltar.aibot.database.dao.commandsDao
import com.helltar.aibot.database.dao.sudoersDao
import com.helltar.aibot.exceptions.ImageTooLargeException
import com.helltar.aibot.utils.HtmlUtils.buildStyledHtmlPage
import io.github.oshai.kotlinlogging.KotlinLogging
import org.telegram.telegrambots.meta.api.methods.ParseMode
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.File
import java.util.concurrent.CompletableFuture

abstract class BotCommand(open val ctx: MessageContext) : Command {

    private companion object {
        const val TELEGRAM_MESSAGE_LENGTH_LIMIT = 4096
        val log = KotlinLogging.logger {}
    }

    val userLanguageCode = ctx.user().languageCode ?: "en"

    protected val userId: Long = ctx.user().id
    protected val message = ctx.message()
    protected val replyMessage: Message? = message.replyToMessage
    protected val isReply = message.isReply && message.replyToMessage.messageId != message.replyToMessage.messageThreadId // todo: tmp. fix, check.
    protected val isNotReply = !isReply

    protected val arguments: Array<String> = ctx.arguments()
    protected val argumentsString: String = ctx.argumentsAsString()

    suspend fun isCommandDisabled(command: String) =
        commandsDao.isDisabled(command)

    suspend fun isChatInAllowlist() =
        chatAllowlistDao.isExists(ctx.chatId())

    suspend fun isUserBanned(userId: Long) =
        banlistDao.isBanned(userId)

    suspend fun isAdmin() =
        sudoersDao.isAdmin(userId)

    fun isCreator(userId: Long) =
        userId == creatorId

    fun isNotMyMessage(message: Message?) =
        message?.from?.userName != telegramBotUsername

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

    protected fun replyToMessageWithPhoto(url: String, caption: String = "", messageId: Int? = message.messageId): Message =
        ctx.replyToMessageWithPhoto()
            .setFile(InputFile(url))
            .setCaption(caption)
            .setReplyToMessageId(messageId)
            .setParseMode(ParseMode.HTML)
            .call(ctx.sender)

    protected fun replyWithTextDocument(text: String, caption: String): Int {
        val title = "replyToMessageId-${message.messageId}"
        val html = buildStyledHtmlPage(title, text.trim()).byteInputStream()

        return ctx.replyWithDocument()
            .setFile("$title.html", html)
            .setCaption(caption)
            .setReplyToMessageId(message.messageId)
            .call(ctx.sender)
            .messageId
    }

    protected fun downloadPhoto(message: Message? = replyMessage, limitBytes: Int = 1024 * 1024): File? {
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
