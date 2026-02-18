package com.helltar.aibot

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService
import com.helltar.aibot.command.CommandNames.Creator.CMD_SLOWMODE
import com.helltar.aibot.command.CommandNames.Creator.CMD_UPDATE_API_KEY
import com.helltar.aibot.command.CommandNames.Creator.CMD_UPDATE_CHAT_MODEL
import com.helltar.aibot.command.CommandNames.Creator.CMD_UPDATE_IMAGE_GEN_MODEL
import com.helltar.aibot.command.CommandNames.User.CMD_CHAT
import com.helltar.aibot.command.CommandNames.User.CMD_CHAT_CTX_REMOVE
import com.helltar.aibot.command.CommandNames.User.CMD_IMAGE_GEN

object Strings {

    object ApiKey {
        const val FAIL_ADD = "⚠\uFE0F Failed to add the <b>%s</b> API key"
        const val FAIL_UPDATE = "⚠\uFE0F Failed to update the <b>%s</b> API key"
        const val BAD_LENGTH = "❌ Invalid API key length"
        const val SUCCESS_ADD = "✅ The API key for <b>%s</b> was added successfully"
        const val SUCCESS_UPDATE = "✅ The API key for <b>%s</b> was updated successfully"
    }

    object Command {
        const val ALREADY_DISABLED = "✅ Command <b>%s</b> is already disabled"
        const val ALREADY_ENABLED = "✅ Command <b>%s</b> is already enabled"
        const val DISABLED = "✅ Command <b>%s</b> has been disabled"
        const val ENABLED = "✅ Command <b>%s</b> has been enabled"
        const val NOT_AVAILABLE = "Command <b>%s</b> is not available. Available: %s"
        const val NOT_SUPPORTED_IN_CHAT = "This command is not supported in this chat \uD83D\uDE48"
        const val TEMPORARILY_DISABLED = "This command is temporarily disabled \uD83D\uDC40"
        const val MANY_CHARACTERS = "Maximum <b>%d</b> characters \uD83D\uDC40"
        const val MANY_REQUEST = "Please wait, I am still processing your previous request \uD83E\uDD16"
        const val ADMIN_ONLY = "You can only use this command as an admin ✋"
        const val CREATOR_CONTEXT_CANNOT_BE_VIEWED = "The creator's context cannot be viewed ✋"
        const val CREATOR_CONTEXT_CANNOT_BE_DELETED = "The creator's context cannot be deleted ✋"
    }

    object Chat {
        const val CONTEXT_EMPTY = "▫\uFE0F No context yet"
        const val CONTEXT_REMOVED = "Context has been cleared \uD83D\uDC4C"
        const val EXCEPTION = "Something went wrong \uD83E\uDEE1"
        const val HELLO = "\uD83D\uDC4B Send <code>/$CMD_CHAT your text</code> or reply to my message to continue the conversation"
        const val MESSAGE_TEXT_NOT_FOUND = "This message does not contain text \uD83E\uDD14"
        const val IMAGE_MUST_BE_LESS_THAN = "The image must be smaller than %s 😥"

        private const val TELEGRAM_API_EXCEPTION = "A Telegram API error occurred."
        private const val TRY_FIX = "You can try to fix it with the /$CMD_CHAT_CTX_REMOVE command ☺\uFE0F"

        const val TELEGRAM_API_EXCEPTION_CONTEXT_SAVED_TO_FILE = "$TELEGRAM_API_EXCEPTION The context was saved to a file. $TRY_FIX"
        const val TELEGRAM_API_EXCEPTION_RESPONSE_SAVED_TO_FILE = "$TELEGRAM_API_EXCEPTION The response was saved to a file. $TRY_FIX"
    }

    object Moderation {
        const val BAN_AND_REASON = "❌ Banned. Reason: <b>%s</b>"
        const val USER_ALREADY_BANNED = "✅ The user is already banned"
        const val USER_BANNED = "❌ The user has been banned"
        const val USER_NOT_BANNED = "✅ The user is not banned"
        const val USER_UNBANNED = "✅ The user has been unbanned"
    }

    object Admins {
        const val ADDED = "✅ Admin added"
        const val EXISTS = "✅ Admin already exists"
        const val REMOVED = "✅ Admin has been removed"
        const val NOT_EXISTS = "❌ Admin does not exist"
    }

    object Allowlist {
        const val CHAT_ADDED = "✅ Chat added"
        const val CHAT_EXISTS = "✅ Chat already exists"
        const val CHAT_REMOVED = "✅ Chat has been removed"
        const val CHAT_NOT_EXISTS = "❌ Chat does not exist"
    }

    object Slowmode {
        const val PLEASE_WAIT = "✋ Slow mode is active. Please wait <b>%d</b> seconds"
        const val SUCCESSFULLY_CHANGED = "✅ Slow mode has been changed to <b>%d</b> requests per hour per user"
        const val CHANGE_FAIL = "❌ Failed to change the slow mode value"
    }

    object Models {
        const val CHAT_SUCCESS_UPDATE = "✅ The chat model was updated to <b>%s</b>"
        const val CHAT_FAIL_UPDATE = "⚠\uFE0F Failed to update the chat model"
        const val IMAGES_SUCCESS_UPDATE = "✅ The image generation model was updated to <b>%s</b>"
        const val IMAGES_FAIL_UPDATE = "⚠\uFE0F Failed to update the image generation model"
        const val BAD_MODEL_NAME_LENGTH = "❌ Invalid model name length"
    }

    object Ui {
        const val LIST_IS_EMPTY = "◻️ List is empty"
    }

    object Templates {
        const val UPDATE_CHAT_MODEL_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ Currently, the chat is using <b>%s</b>. To change it, use the command like this:
        
        <code>/$CMD_UPDATE_CHAT_MODEL</code> gpt-5.2
        """

        const val UPDATE_IMAGES_MODEL_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ Currently, the image generation is using <b>%s</b>. To change it, use the command like this:
        
        <code>/$CMD_UPDATE_IMAGE_GEN_MODEL</code> gpt-image-1.5
        """

        const val SLOWMODE_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ The current value is <b>%d</b> requests per hour per user.
        
        To change it, use the command <code>/$CMD_SLOWMODE</code> 15
        """

        const val UPDATE_API_KEY_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ How to use:
        
        <code>/$CMD_UPDATE_API_KEY</code> sk-proj-qwertyuiop
        """

        const val IMG_GEN_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ How to use:
        
        <code>/$CMD_IMAGE_GEN</code> Generate an image of gray tabby cat hugging an otter with an orange scarf
        """
    }

    object LocalizationKeys {
        const val CHAT_WAIT_MESSAGE = "long_running_command_message"
        const val VISION_DEFAULT_PROMPT = "vision_default_prompt"
    }

    private val localization = ResourceBundleLocalizationService("language")

    fun localizedString(key: String, languageCode: String): String =
        localization.getString(key, languageCode)
}
