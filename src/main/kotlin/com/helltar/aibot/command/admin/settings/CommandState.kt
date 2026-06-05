package com.helltar.aibot.command.admin.settings

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.database.dao.commandsDao

class CommandState(ctx: BotCommandContext, private val disable: Boolean = false) : BotCommand(ctx) {

    private companion object {
        const val ENABLED_SYMBOL = """🟢"""
        const val DISABLED_SYMBOL = """⚪️"""
    }

    override suspend fun run() {
        if (arguments.isEmpty()) {
            replyToMessage(getCommandsStatusText())
            return
        }

        val commandName = arguments[0]

        if (!CommandNames.toggleableCommands.contains(commandName)) {
            val formattedCommands = CommandNames.toggleableCommands.joinToString { "<code>$it</code>" }
            replyToMessage(BotMessages.Command.notAvailable(commandName, formattedCommands))
            return
        }

        if (!disable)
            enable(commandName)
        else
            disable(commandName)
    }

    override fun commandName() =
        if (disable)
            CommandNames.Admin.CMD_DISABLE
        else
            CommandNames.Admin.CMD_ENABLE

    private suspend fun getCommandsStatusText() =
        CommandNames.toggleableCommands.map { commandName ->
            val isDisabled = commandsDao.isDisabled(commandName)
            val status = if (isDisabled) DISABLED_SYMBOL else ENABLED_SYMBOL
            "$status <code>$commandName</code>"
        }
            .sortedDescending()
            .joinToString("\n")

    private suspend fun enable(commandName: String) {
        if (!commandsDao.isDisabled(commandName))
            replyToMessage(BotMessages.Command.alreadyEnabled(commandName))
        else {
            commandsDao.changeState(commandName, false)
            replyToMessage(BotMessages.Command.enabled(commandName))
        }
    }

    private suspend fun disable(commandName: String) {
        if (commandsDao.isDisabled(commandName))
            replyToMessage(BotMessages.Command.alreadyDisabled(commandName))
        else {
            commandsDao.changeState(commandName, true)
            replyToMessage(BotMessages.Command.disabled(commandName))
        }
    }
}
