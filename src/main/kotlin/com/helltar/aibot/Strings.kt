package com.helltar.aibot

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService
import com.helltar.aibot.commands.Commands.Creator.CMD_SLOWMODE
import com.helltar.aibot.commands.Commands.Creator.CMD_UPDATE_API_KEY
import com.helltar.aibot.commands.Commands.Creator.CMD_UPDATE_CHAT_MODEL
import com.helltar.aibot.commands.Commands.User.CMD_CHAT_CTX_REMOVE

object Strings {

    const val API_KEY_FAIL_ADD = "⚠\uFE0F Error when add <b>%s</b> API Key"
    const val API_KEY_FAIL_UPDATE = "⚠\uFE0F Error when update <b>%s</b> API Key"
    const val BAD_API_KEY_LENGTH = "❌ Invalid API key length"
    const val BAN_AND_REASON = "❌ Ban, reason: <b>%s</b>"
    const val CHAT_CONTEXT_EMPTY = "▫\uFE0F Empty"
    const val CHAT_CONTEXT_REMOVED = "Context has been removed \uD83D\uDC4C"
    const val CHAT_EXCEPTION = "Something went wrong \uD83E\uDEE1"
    const val CHAT_HELLO = "\uD83D\uDC4B Hello, please ask your questions as replying to my messages"
    const val COMMAND_ALREADY_DISABLED = "✅ Command <b>%s</b> already disabled"
    const val COMMAND_ALREADY_ENABLED = "✅ Command <b>%s</b> already enabled"
    const val COMMAND_DISABLED = "✅ Command <b>%s</b> disabled"
    const val COMMAND_ENABLED = "✅ Command <b>%s</b> enabled"
    const val COMMAND_NOT_AVAILABLE = "Command <b>%s</b> is not available. Available: %s"
    const val COMMAND_NOT_SUPPORTED_IN_CHAT = "Command is not supported in this chat \uD83D\uDE48"
    const val COMMAND_TEMPORARY_DISABLED = "Command temporary disabled \uD83D\uDC40"
    const val IMAGE_MUST_BE_LESS_THAN = "Image must be less than %s 😥"
    const val LIST_IS_EMPTY = "◻️ List is empty"
    const val MANY_CHARACTERS = "Max <b>%d</b> characters \uD83D\uDC40"
    const val MANY_REQUEST = "Wait, let me deal with the last request \uD83E\uDD16"
    const val MESSAGE_TEXT_NOT_FOUND = "The message does not contain text \uD83E\uDD14"
    const val PROVIDER_API_KEY_SUCCESS_ADD = "✅ API Key for <b>%s</b> succesfully added"
    const val PROVIDER_API_KEY_SUCCESS_UPDATE = "✅ API Key for <b>%s</b> succesfully updated"

    private const val TELEGRAM_API_EXCEPTION = "An error occurred (TelegramApiException)."
    private const val TRY_FIX = "You can try fixing it using the /$CMD_CHAT_CTX_REMOVE command ☺\uFE0F"
    const val TELEGRAM_API_EXCEPTION_CONTEXT_SAVED_TO_FILE = "$TELEGRAM_API_EXCEPTION The context has been saved to a file. $TRY_FIX"
    const val TELEGRAM_API_EXCEPTION_RESPONSE_SAVED_TO_FILE = "$TELEGRAM_API_EXCEPTION The response has been saved to a file. $TRY_FIX"

    const val USER_ALREADY_BANNED = "✅ User already banned"
    const val USER_BANNED = "❌ User banned"
    const val USER_NOT_BANNED = "✅ User not banned"
    const val USER_UNBANNED = "✅ User unbanned"

    const val ADMIN_ADDED = "✅ Admin added"
    const val ADMIN_EXISTS = "✅ Admin already exists"
    const val ADMIN_REMOVED = "✅ Admin has been removed"
    const val ADMIN_NOT_EXISTS = "❌ Admin does not exist"

    const val CHAT_ADDED = "✅ Chat added"
    const val CHAT_EXISTS = "✅ Chat already exists"
    const val CHAT_REMOVED = "✅ Chat has been removed"
    const val CHAT_NOT_EXISTS = "❌ Chat does not exist"

    const val CHAT_WAIT_MESSAGE = "long_running_command_message"
    const val VISION_DEFAULT_PROMPT = "vision_default_prompt"

    const val ADMIN_ONLY_COMMAND = "You cannot use this command like this (admin-only) ✋"
    const val CREATOR_CONTEXT_CANNOT_BE_VIEWED = "Creator context cannot be viewed ✋"
    const val CREATOR_CONTEXT_CANNOT_BE_DELETED = "Creator context cannot be deleted ✋"

    const val SLOWMODE_PLEASE_WAIT = "✋ Slowmode, wait <b>%d</b> seconds"
    const val SLOWMODE_SUCCESFULLY_CHANGED = "✅ The value for system-slowmode has been successfully changed to <b>%d</b> requests per hour per user."
    const val SLOWMODE_CHANGE_FAIL = "❌ Error when change system-slowmode value"

    const val CHAT_MODEL_SUCCESS_UPDATE = "✅ Chat model has been successfully updated to <b>%s</b>"
    const val CHAT_MODEL_FAIL_UPDATE = "⚠\uFE0F Error while updating chat model"
    const val IMAGES_MODEL_SUCCESS_UPDATE = "✅ Model for images generations has been successfully updated to <b>%s</b>"
    const val IMAGES_MODEL_FAIL_UPDATE = "⚠\uFE0F Error while updating model for images generations"
    const val BAD_MODEL_NAME_LENGTH = "❌ Incorrect model name length"

    const val UPDATE_CHAT_MODEL_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ Currently, the chat is using <b>%s</b>. To change it, use the command like this:
        
        <code>/$CMD_UPDATE_CHAT_MODEL</code> gpt-4.1
        """

    const val SLOWMODE_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ The current value is <b>%d</b> requests per hour per user.
        
        To change it, use the command <code>/$CMD_SLOWMODE</code> <u>15</u>
        """

    const val UPDATE_API_KEY_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ How to use:
        
        <code>/$CMD_UPDATE_API_KEY</code> <u>sk-proj-qwertyuiop</u>
        """

    private val localization = ResourceBundleLocalizationService("language")

    fun localizedString(key: String, languageCode: String): String =
        localization.getString(key, languageCode)
}
