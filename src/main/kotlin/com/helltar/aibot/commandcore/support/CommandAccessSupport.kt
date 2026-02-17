package com.helltar.aibot.commandcore.support

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Config.creatorId
import com.helltar.aibot.Config.telegramBotUsername
import com.helltar.aibot.database.dao.banlistDao
import com.helltar.aibot.database.dao.chatAllowlistDao
import com.helltar.aibot.database.dao.commandsDao
import com.helltar.aibot.database.dao.sudoersDao
import org.telegram.telegrambots.meta.api.objects.message.Message

class CommandAccessSupport(private val ctx: MessageContext, private val userId: Long) {

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
}
