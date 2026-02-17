package com.helltar.aibot

import com.annimon.tgbotsmodule.services.ResourceBundleLocalizationService
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_SLOWMODE
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_UPDATE_API_KEY
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_UPDATE_CHAT_MODEL
import com.helltar.aibot.commandcore.CommandNames.Creator.CMD_UPDATE_IMAGE_GEN_MODEL
import com.helltar.aibot.commandcore.CommandNames.User.CMD_CHAT_CTX_REMOVE
import com.helltar.aibot.commandcore.CommandNames.User.CMD_IMAGE_GEN

object Strings {

    object ApiKey {
        const val FAIL_ADD = "⚠\uFE0F Error while adding <b>%s</b> API key"
        const val FAIL_UPDATE = "⚠\uFE0F Error while updating <b>%s</b> API key"
        const val BAD_LENGTH = "❌ Invalid API key length"
        const val SUCCESS_ADD = "✅ API key for <b>%s</b> successfully added"
        const val SUCCESS_UPDATE = "✅ API key for <b>%s</b> successfully updated"
    }

    object Command {
        const val ALREADY_DISABLED = "✅ Command <b>%s</b> already disabled"
        const val ALREADY_ENABLED = "✅ Command <b>%s</b> already enabled"
        const val DISABLED = "✅ Command <b>%s</b> disabled"
        const val ENABLED = "✅ Command <b>%s</b> enabled"
        const val NOT_AVAILABLE = "Command <b>%s</b> is not available. Available: %s"
        const val NOT_SUPPORTED_IN_CHAT = "Command is not supported in this chat \uD83D\uDE48"
        const val TEMPORARILY_DISABLED = "Command is temporarily disabled \uD83D\uDC40"
        const val MANY_CHARACTERS = "Max <b>%d</b> characters \uD83D\uDC40"
        const val MANY_REQUEST = "Wait, let me deal with the last request \uD83E\uDD16"
        const val ADMIN_ONLY = "You cannot use this command like this (admin-only) ✋"
        const val CREATOR_CONTEXT_CANNOT_BE_VIEWED = "Creator context cannot be viewed ✋"
        const val CREATOR_CONTEXT_CANNOT_BE_DELETED = "Creator context cannot be deleted ✋"
    }

    object Chat {
        const val CONTEXT_EMPTY = "▫\uFE0F Empty"
        const val CONTEXT_REMOVED = "Context has been removed \uD83D\uDC4C"
        const val EXCEPTION = "Something went wrong \uD83E\uDEE1"
        const val HELLO = "\uD83D\uDC4B Hello, please ask your questions as replying to my messages"
        const val MESSAGE_TEXT_NOT_FOUND = "The message does not contain text \uD83E\uDD14"
        const val IMAGE_MUST_BE_LESS_THAN = "Image must be less than %s 😥"

        private const val TELEGRAM_API_EXCEPTION = "An error occurred (TelegramApiException)."
        private const val TRY_FIX = "You can try fixing it using the /$CMD_CHAT_CTX_REMOVE command ☺\uFE0F"
        const val TELEGRAM_API_EXCEPTION_CONTEXT_SAVED_TO_FILE = "$TELEGRAM_API_EXCEPTION The context has been saved to a file. $TRY_FIX"
        const val TELEGRAM_API_EXCEPTION_RESPONSE_SAVED_TO_FILE = "$TELEGRAM_API_EXCEPTION The response has been saved to a file. $TRY_FIX"
    }

    object Moderation {
        const val BAN_AND_REASON = "❌ Ban, reason: <b>%s</b>"
        const val USER_ALREADY_BANNED = "✅ User already banned"
        const val USER_BANNED = "❌ User banned"
        const val USER_NOT_BANNED = "✅ User not banned"
        const val USER_UNBANNED = "✅ User unbanned"
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
        const val PLEASE_WAIT = "✋ Slowmode, wait <b>%d</b> seconds"
        const val SUCCESSFULLY_CHANGED = "✅ The system slowmode value has been changed to <b>%d</b> requests per hour per user."
        const val CHANGE_FAIL = "❌ Error while changing the system slowmode value"
    }

    object Models {
        const val CHAT_SUCCESS_UPDATE = "✅ Chat model has been successfully updated to <b>%s</b>"
        const val CHAT_FAIL_UPDATE = "⚠\uFE0F Error while updating chat model"
        const val IMAGES_SUCCESS_UPDATE = "✅ Image generation model has been successfully updated to <b>%s</b>"
        const val IMAGES_FAIL_UPDATE = "⚠\uFE0F Error while updating the image generation model"
        const val BAD_MODEL_NAME_LENGTH = "❌ Incorrect model name length"
    }

    object Ui {
        const val LIST_IS_EMPTY = "◻️ List is empty"
    }

    object Templates {
        const val UPDATE_CHAT_MODEL_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ Currently, the chat is using <b>%s</b>. To change it, use the command like this:
        
        <code>/$CMD_UPDATE_CHAT_MODEL</code> gpt-4.1
        """

        const val UPDATE_IMAGES_MODEL_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ Currently, the image generation is using <b>%s</b>. To change it, use the command like this:
        
        <code>/$CMD_UPDATE_IMAGE_GEN_MODEL</code> dall-e-3
        """

        const val SLOWMODE_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ The current value is <b>%d</b> requests per hour per user.
        
        To change it, use the command <code>/$CMD_SLOWMODE</code> <u>15</u>
        """

        const val UPDATE_API_KEY_COMMAND_USAGE_TEMPLATE_RAW = """
        ℹ️ How to use:
        
        <code>/$CMD_UPDATE_API_KEY</code> <u>sk-proj-qwertyuiop</u>
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
