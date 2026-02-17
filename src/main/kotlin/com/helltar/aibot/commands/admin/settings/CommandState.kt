package com.helltar.aibot.commands.admin.settings

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.commands.Commands
import com.helltar.aibot.commands.BotCommand
import com.helltar.aibot.Strings
import com.helltar.aibot.database.dao.commandsDao

class CommandState(ctx: MessageContext, private val disable: Boolean = false) : BotCommand(ctx) {

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

        if (!Commands.disableableCommands.contains(commandName)) {
            val formattedCommands = Commands.disableableCommands.joinToString { "<code>$it</code>" }
            replyToMessage(Strings.COMMAND_NOT_AVAILABLE.format(commandName, formattedCommands))
            return
        }

        if (!disable)
            enable(commandName)
        else
            disable(commandName)
    }

    override fun commandName() =
        if (disable)
            Commands.Admin.CMD_DISABLE
        else
            Commands.Admin.CMD_ENABLE

    private suspend fun getCommandsStatusText() =
        Commands.disableableCommands.map { commandName ->
            val isDisabled = commandsDao.isDisabled(commandName)
            val status = if (isDisabled) DISABLED_SYMBOL else ENABLED_SYMBOL
            "$status <code>$commandName</code>"
        }
            .sortedDescending()
            .joinToString("\n")

    private suspend fun enable(commandName: String) {
        if (!commandsDao.isDisabled(commandName))
            replyToMessage(Strings.COMMAND_ALREADY_ENABLED.format(commandName))
        else {
            commandsDao.changeState(commandName, false)
            replyToMessage(Strings.COMMAND_ENABLED.format(commandName))
        }
    }

    private suspend fun disable(commandName: String) {
        if (commandsDao.isDisabled(commandName))
            replyToMessage(Strings.COMMAND_ALREADY_DISABLED.format(commandName))
        else {
            commandsDao.changeState(commandName, true)
            replyToMessage(Strings.COMMAND_DISABLED.format(commandName))
        }
    }
}
