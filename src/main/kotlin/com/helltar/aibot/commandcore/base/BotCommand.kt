package com.helltar.aibot.commandcore.base

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commandcore.support.CommandAccessSupport
import com.helltar.aibot.commandcore.support.CommandMessageSupport
import org.telegram.telegrambots.meta.api.objects.message.Message
import java.io.File
import java.util.concurrent.CompletableFuture

abstract class BotCommand(val ctx: MessageContext) : BaseCommand(ctx) {

    private val accessSupport = CommandAccessSupport(ctx, userId)
    private val messageSupport = CommandMessageSupport(ctx, message, replyMessage)

    suspend fun isCommandDisabled(command: String) =
        accessSupport.isCommandDisabled(command)

    suspend fun isChatInAllowlist() =
        accessSupport.isChatInAllowlist()

    suspend fun isUserBanned(userId: Long) =
        accessSupport.isUserBanned(userId)

    suspend fun isAdmin() =
        accessSupport.isAdmin()

    protected fun isCreator(userId: Long) =
        accessSupport.isCreator(userId)

    protected fun isNotMyMessage(message: Message?) =
        accessSupport.isNotMyMessage(message)

    fun replyToMessage(text: String, messageId: Int = message.messageId, webPagePreview: Boolean = false) {
        messageSupport.replyToMessage(text, messageId, webPagePreview)
    }

    fun replyToMessageWithDocument(fileId: String, caption: String): Int =
        messageSupport.replyToMessageWithDocument(fileId, caption)

    fun deleteMessage(messageId: Int): CompletableFuture<Boolean> =
        messageSupport.deleteMessage(messageId)

    fun sendDocument(file: File): Message =
        messageSupport.sendDocument(file)

    protected fun replyToMessageWithPhoto(bytes: ByteArray, caption: String, messageId: Int? = message.messageId): Message =
        messageSupport.replyToMessageWithPhoto(bytes, caption, messageId)

    protected fun replyWithTextDocument(text: String, caption: String): Int =
        messageSupport.replyWithTextDocument(text, caption)

    protected fun downloadPhoto(message: Message? = replyMessage, limitBytes: Int): File? =
        messageSupport.downloadPhoto(message, limitBytes)
}
