package com.helltar.aibot.commandcore.base

import com.annimon.tgbotsmodule.commands.context.MessageContext
import org.telegram.telegrambots.meta.api.objects.message.Message

abstract class BaseCommand(open val ctx: MessageContext) {

    val userLanguageCode = ctx.user().languageCode ?: "en"

    protected val userId: Long = ctx.user().id
    protected val message = ctx.message()
    protected val replyMessage: Message? = message.replyToMessage
    protected val isReply = message.isReply && message.replyToMessage.messageId != message.replyToMessage.messageThreadId // todo: tmp. fix, check.
    protected val isNotReply = !isReply

    protected val arguments: Array<String> = ctx.arguments()
    protected val argumentsString: String = ctx.argumentsAsString()

    abstract suspend fun run()
    abstract fun commandName(): String
}
