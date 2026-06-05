package com.helltar.aibot.command

import com.helltar.aibot.Strings
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.banlistDao
import com.helltar.aibot.database.dao.configurationsDao
import com.helltar.aibot.database.dao.slowmodeDao
import com.helltar.aibot.utils.DateTimeUtils.instantNow
import com.helltar.aibot.utils.StringUtils.singleLineTruncated
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class CommandExecutor(private val creatorId: Long) {

    private companion object {
        const val SLOW_MODE_TIMEOUT_HOURS = 1
        const val CHAT_ACTION_INTERVAL_SECONDS = 4
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
                    val isCreator = userId == creatorId
                    val isAdmin = botCommand.isAdmin()

                    val shouldRunCommand =
                        when {
                            !options.checkRights -> true
                            options.isCreatorCommand -> isCreator
                            options.isAdminCommand -> isAdmin
                            isCreator || isAdmin -> true
                            else -> canExecuteCommand(botCommand)
                        }

                    if (shouldRunCommand)
                        runCommand(botCommand, options.isLongRunningCommand)
                    else
                        log.info { "command /$commandName not allowed for user $userId" }
                } catch (e: Exception) {
                    log.error(e) { "failed to execute command /$commandName for user $userId" }
                } finally {
                    requestsMap.remove(requestKey)
                }
            }
    }

    private fun logCommandExecution(botCommand: BotCommand, user: User, chat: Chat, commandName: String) {
        val chatInfo = chat.title?.let { "${chat.id} '$it'" } ?: chat.id.toString()
        val userInfo = listOfNotNull(user.id, user.userName?.let { "@$it" }, user.firstName).joinToString(" ")
        val text = botCommand.ctx.message().text.orEmpty().singleLineTruncated(500)
        log.info { "/$commandName | chat: $chatInfo | user: $userInfo | text: $text" }
    }

    private fun isRequestInProgress(requestKey: String, botCommand: BotCommand) =
        if (requestsMap.containsKey(requestKey) && requestsMap[requestKey]?.isCompleted == false) {
            botCommand.replyToMessage(Strings.Command.MANY_REQUEST)
            true
        } else
            false

    private suspend fun canExecuteCommand(botCommand: BotCommand): Boolean {
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

        return passesSlowmode(botCommand)
    }

    private suspend fun passesSlowmode(botCommand: BotCommand): Boolean {
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
        if (!isLongRunningCommand) {
            botCommand.run()
            return
        }

        val chatActionJob =
            scope.launch {
                while (isActive) {
                    botCommand.sendChatAction()
                    delay(CHAT_ACTION_INTERVAL_SECONDS.seconds)
                }
            }

        try {
            botCommand.run()
        } finally {
            chatActionJob.cancel()
        }
    }

    private suspend fun getSlowmodeRemainingSeconds(userId: Long): Long {
        val userSlowmodeStatus = slowmodeDao.slowmodeStatus(userId)

        if (userSlowmodeStatus == null) {
            slowmodeDao.registerUser(userId)
            return 0
        }

        val lastUsage = userSlowmodeStatus.lastUsage
        val timeElapsed = Duration.between(lastUsage, instantNow())

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
}
