package com.helltar.aibot.commands.admin.settings

import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
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

        if (!CommandNames.toggleableCommands.contains(commandName)) {
            val formattedCommands = CommandNames.toggleableCommands.joinToString { "<code>$it</code>" }
            replyToMessage(Strings.Command.NOT_AVAILABLE.format(commandName, formattedCommands))
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
            replyToMessage(Strings.Command.ALREADY_ENABLED.format(commandName))
        else {
            commandsDao.changeState(commandName, false)
            replyToMessage(Strings.Command.ENABLED.format(commandName))
        }
    }

    private suspend fun disable(commandName: String) {
        if (commandsDao.isDisabled(commandName))
            replyToMessage(Strings.Command.ALREADY_DISABLED.format(commandName))
        else {
            commandsDao.changeState(commandName, true)
            replyToMessage(Strings.Command.DISABLED.format(commandName))
        }
    }
}
