package com.helltar.aibot.bot

import com.annimon.tgbotsmodule.BotHandler
import com.annimon.tgbotsmodule.BotModuleOptions
import com.annimon.tgbotsmodule.commands.CommandRegistry
import com.annimon.tgbotsmodule.commands.SimpleCommand
import com.annimon.tgbotsmodule.commands.authority.SimpleAuthority
import com.annimon.tgbotsmodule.commands.context.MessageContext
import com.helltar.aibot.Config.creatorId
import com.helltar.aibot.Config.telegramBotUsername
import com.helltar.aibot.commandcore.CommandExecutor
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_ADMIN_LIST
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_BAN_LIST
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_BAN_USER
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_CHAT_ALLOW_LIST
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_DISABLE
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_ENABLE
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_RM_ADMIN
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_RM_CHAT
import com.helltar.aibot.commandcore.CommandNames.Admin.CMD_UNBAN_USER
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_ADD_ADMIN
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_ADD_CHAT
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_SLOWMODE
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_UPDATE_API_KEY
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_UPDATE_CHAT_MODEL
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_UPDATE_IMAGE_GEN_MODEL
import com.helltar.aibot.commandcore.CommandNames.General.CMD_ABOUT
import com.helltar.aibot.commandcore.CommandNames.General.CMD_MYID
import com.helltar.aibot.commandcore.CommandNames.General.CMD_START
import com.helltar.aibot.commandcore.CommandNames.User.CMD_CHAT
import com.helltar.aibot.commandcore.CommandNames.User.CMD_CHATCTX
import com.helltar.aibot.commandcore.CommandNames.User.CMD_CHAT_CTX_REMOVE
import com.helltar.aibot.commandcore.CommandNames.User.CMD_IMAGE_GEN
import com.helltar.aibot.commandcore.CommandOptions
import com.helltar.aibot.commandcore.base.BotCommand
import com.helltar.aibot.commands.admin.allowlist.AddChat
import com.helltar.aibot.commands.admin.allowlist.ChatAllowlist
import com.helltar.aibot.commands.admin.allowlist.RemoveChat
import com.helltar.aibot.commands.admin.moderation.BanUser
import com.helltar.aibot.commands.admin.moderation.Banlist
import com.helltar.aibot.commands.admin.moderation.UnbanUser
import com.helltar.aibot.commands.admin.settings.*
import com.helltar.aibot.commands.admin.sudoers.AddAdmin
import com.helltar.aibot.commands.admin.sudoers.AdminList
import com.helltar.aibot.commands.admin.sudoers.RemoveAdmin
import com.helltar.aibot.commands.chat.Chat
import com.helltar.aibot.commands.chat.ChatCtx
import com.helltar.aibot.commands.chat.ChatCtxRemove
import com.helltar.aibot.commands.general.About
import com.helltar.aibot.commands.general.MyId
import com.helltar.aibot.commands.general.Start
import com.helltar.aibot.commands.media.ImageGen
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod
import org.telegram.telegrambots.meta.api.objects.EntityType
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

class ArtificIntelligBotHandler(botModuleOptions: BotModuleOptions) : BotHandler(botModuleOptions) {

    private val authority = SimpleAuthority(creatorId)
    private val registry = CommandRegistry(telegramBotUsername, authority)

    private val commandExecutor = CommandExecutor()

    init {
        registerSimpleCommand(CMD_START, ::Start)
        registerSimpleCommand(CMD_MYID, ::MyId)
        registerSimpleCommand(CMD_ABOUT, ::About)
        registerSimpleCommand(CMD_CHATCTX, ::ChatCtx, checkRights = true)
        registerSimpleCommand(CMD_CHAT_CTX_REMOVE, ::ChatCtxRemove, checkRights = true)

        registerLongRunningCommand(CMD_CHAT, ::Chat)
        registerLongRunningCommand(CMD_IMAGE_GEN, ::ImageGen)

        registerAdminCommand(CMD_ENABLE, ::CommandState)
        registerAdminCommand(CMD_DISABLE, { CommandState(it, disable = true) })
        registerAdminCommand(CMD_BAN_LIST, ::Banlist)
        registerAdminCommand(CMD_BAN_USER, ::BanUser)
        registerAdminCommand(CMD_UNBAN_USER, ::UnbanUser)
        registerAdminCommand(CMD_RM_ADMIN, ::RemoveAdmin)
        registerAdminCommand(CMD_RM_CHAT, ::RemoveChat)
        registerAdminCommand(CMD_ADMIN_LIST, ::AdminList, privateChatOnly = true)
        registerAdminCommand(CMD_CHAT_ALLOW_LIST, ::ChatAllowlist, privateChatOnly = true)

        registerCreatorCommand(CMD_ADD_ADMIN, ::AddAdmin)
        registerCreatorCommand(CMD_ADD_CHAT, ::AddChat)
        registerCreatorCommand(CMD_SLOWMODE, ::SlowmodeSetting)
        registerCreatorCommand(CMD_UPDATE_API_KEY, ::UpdateApiKey, privateChatOnly = true)
        registerCreatorCommand(CMD_UPDATE_CHAT_MODEL, ::UpdateChatModel)
        registerCreatorCommand(CMD_UPDATE_IMAGE_GEN_MODEL, ::UpdateImageGenModel)
    }

    override fun onUpdate(update: Update): BotApiMethod<*>? {

        fun executeCommand(botCommand: BotCommand) =
            commandExecutor.execute(botCommand, createCommandOptions(isLongRunningCommand = true))

        fun shouldProcessMessage(replyToMessage: Message, text: String): Boolean {
            val isMe = replyToMessage.from.userName == telegramBotUsername
            return isMe && !replyToMessage.hasPhoto() && !text.startsWith("/")
        }

        fun processMessage(message: Message) {
            if (!message.hasEntities() ||
                !message.entities.any { it.type == EntityType.MENTION || it.type == EntityType.TEXTMENTION }
            ) {
                val ctx = MessageContext(this, update, "")

                if (!message.replyToMessage.hasAudio() && !message.replyToMessage.hasVoice())
                    executeCommand(Chat(ctx))
            }
        }

        if (update.hasMessage() && update.message.isReply && update.message.hasText()) {
            val message = update.message

            if (shouldProcessMessage(message.replyToMessage, message.text))
                processMessage(message)
        }

        registry.handleUpdate(this, update)

        return null
    }

    override fun handleTelegramApiException(tae: TelegramApiException?) {
        throw tae ?: TelegramApiException("TelegramApiException") // todo: TelegramApiException
    }

    private fun createCommandOptions(
        checkRights: Boolean = true,
        isAdminCommand: Boolean = false,
        isCreatorCommand: Boolean = false,
        isLongRunningCommand: Boolean = false,
        privateChatOnly: Boolean = false
    ) =
        CommandOptions(checkRights, isAdminCommand, isCreatorCommand, isLongRunningCommand, privateChatOnly)

    private fun registerCommand(command: String, botCommand: (MessageContext) -> BotCommand, options: CommandOptions) {
        registry.register(SimpleCommand("/$command") { commandExecutor.execute(botCommand(it), options) })
    }

    private fun registerSimpleCommand(command: String, botCommand: (MessageContext) -> BotCommand, checkRights: Boolean = false) {
        registerCommand(command, botCommand, createCommandOptions(checkRights = checkRights))
    }

    private fun registerLongRunningCommand(command: String, botCommand: (MessageContext) -> BotCommand) {
        registerCommand(command, botCommand, createCommandOptions(isLongRunningCommand = true))
    }

    private fun registerAdminCommand(command: String, botCommand: (MessageContext) -> BotCommand, privateChatOnly: Boolean = false) {
        registerCommand(command, botCommand, createCommandOptions(isAdminCommand = true, privateChatOnly = privateChatOnly))
    }

    private fun registerCreatorCommand(command: String, botCommand: (MessageContext) -> BotCommand, privateChatOnly: Boolean = false) {
        registerCommand(command, botCommand, createCommandOptions(isCreatorCommand = true, privateChatOnly = privateChatOnly))
    }
}
