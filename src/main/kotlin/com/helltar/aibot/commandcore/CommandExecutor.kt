package com.helltar.aibot.commandcore

import com.helltar.aibot.Config
import com.helltar.aibot.Config.LOADING_ANIMATION_FILE
import com.helltar.aibot.Strings
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.database.dao.banlistDao
import com.helltar.aibot.database.dao.configurationsDao
import com.helltar.aibot.database.dao.slowmodeDao
import com.helltar.aibot.utils.DateTimeUtils.utcNow
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import java.io.File
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.hours

class CommandExecutor {

    private companion object {
        const val SLOW_MODE_TIMEOUT_HOURS = 1
        val scope = CoroutineScope(Dispatchers.IO)
        val requestsMap = ConcurrentHashMap<String, Job>()
        val log = KotlinLogging.logger {}
    }

    fun execute(botCommand: BotCommand, options: CommandOptions) {
        val user = botCommand.ctx.user()
        val userId = user.id
        val chat = botCommand.ctx.message().chat
        val commandName = botCommand.commandName()

        logCommandExecution(botCommand, user, chat, commandName)

        val requestKey = "$commandName@$userId"

        if (isRequestInProgress(requestKey, botCommand)) return

        requestsMap[requestKey] =
            scope.launch {
                if (options.privateChatOnly && !chat.isUserChat) return@launch

                try {
                    val isCreator = userId == Config.botConfig.creatorId
                    val isAdmin = botCommand.isAdmin()

                    val shouldRunCommand =
                        when {
                            !options.checkRights -> true
                            options.isCreatorCommand -> isCreator
                            options.isAdminCommand -> isAdmin
                            isCreator || isAdmin -> true
                            else -> isCanExecuteCommand(botCommand)
                        }

                    if (shouldRunCommand)
                        runCommand(botCommand, options.isLongRunningCommand)
                    else
                        log.info { "command /$commandName not allowed for user $userId" }
                } catch (e: Exception) {
                    log.error { e.message }
                } finally {
                    requestsMap.remove(requestKey)
                }
            }
    }

    private fun logCommandExecution(botCommand: BotCommand, user: User, chat: Chat, commandName: String) {
        val logMessage = "$commandName: ${chat.id} ${chat.title} ${user.id} ${user.userName} ${user.firstName}: ${botCommand.ctx.message().text}"
        log.info { logMessage }
    }

    private fun isRequestInProgress(requestKey: String, botCommand: BotCommand) =
        if (requestsMap.containsKey(requestKey) && requestsMap[requestKey]?.isCompleted == false) {
            botCommand.replyToMessage(Strings.Command.MANY_REQUEST)
            true
        } else
            false

    private suspend fun isCanExecuteCommand(botCommand: BotCommand): Boolean {
        val userId = botCommand.ctx.user().id

        if (botCommand.isUserBanned(userId)) {
            val reason = banlistDao.reason(userId) ?: """🤷‍♂️"""
            botCommand.replyToMessage(Strings.Moderation.BAN_AND_REASON.format(reason))
            return false
        }

        if (!botCommand.isChatInAllowlist()) {
            botCommand.replyToMessage(Strings.Command.NOT_SUPPORTED_IN_CHAT)
            return false
        }

        val commandName = botCommand.commandName()

        if (botCommand.isCommandDisabled(commandName)) {
            botCommand.replyToMessage(Strings.Command.TEMPORARILY_DISABLED)
            return false
        }

        return isNotInSlowmode(botCommand)
    }

    private suspend fun isNotInSlowmode(botCommand: BotCommand): Boolean {
        if (botCommand.commandName() in CommandNames.toggleableCommands) {
            val slowmodeRemainingSeconds = getSlowmodeRemainingSeconds(botCommand.ctx.user().id)

            if (slowmodeRemainingSeconds > 0) {
                botCommand.replyToMessage(Strings.Slowmode.PLEASE_WAIT.format(slowmodeRemainingSeconds))
                return false
            }
        }

        return true
    }

    private suspend fun runCommand(botCommand: BotCommand, isLongRunningCommand: Boolean) {
        if (isLongRunningCommand) {
            val caption = Strings.localizedString(Strings.LocalizationKeys.CHAT_WAIT_MESSAGE, botCommand.userLanguageCode)
            val messageId = sendWaitingGif(botCommand, caption)

            try {
                botCommand.run()
            } finally {
                botCommand.deleteMessage(messageId)
            }
        } else
            botCommand.run()
    }

    private suspend fun getSlowmodeRemainingSeconds(userId: Long): Long {
        val userSlowmodeStatus = slowmodeDao.slowmodeStatus(userId)

        if (userSlowmodeStatus == null) {
            slowmodeDao.registerUser(userId)
            return 0
        }

        val lastUsage = userSlowmodeStatus.lastUsage
        val timeElapsed = Duration.between(lastUsage, utcNow())

        if (timeElapsed.toHours() >= SLOW_MODE_TIMEOUT_HOURS) {
            slowmodeDao.resetUsageCount(userId)
            return 0
        }

        val slowmodeMaxUsageCount = configurationsDao.slowmodeMaxUsageCount()

        if (userSlowmodeStatus.usageCount >= slowmodeMaxUsageCount)
            return SLOW_MODE_TIMEOUT_HOURS.hours.inWholeSeconds - timeElapsed.seconds

        slowmodeDao.incrementUsageCount(userId)

        return 0
    }

    private suspend fun sendWaitingGif(botCommand: BotCommand, caption: String): Int {

        suspend fun sendGifAndUpdateFileId(): Int {
            val message = botCommand.sendDocument(File(LOADING_ANIMATION_FILE))
            val fileId = message.document.fileId

            if (fileId != null)
                configurationsDao.updateLoadingGifFileId(fileId)

            return message.messageId
        }

        val fileId = configurationsDao.loadingGifFileId()

        return if (fileId != null) {
            try {
                botCommand.replyToMessageWithDocument(fileId, caption)
            } catch (e: Exception) {
                log.error { e.message }
                sendGifAndUpdateFileId()
            }
        } else
            sendGifAndUpdateFileId()
    }
}
