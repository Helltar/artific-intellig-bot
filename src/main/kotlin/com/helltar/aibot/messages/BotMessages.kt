package com.helltar.aibot.messages

import com.helltar.aibot.command.CommandNames.Creator.CMD_SLOWMODE
import com.helltar.aibot.command.CommandNames.Creator.CMD_UPDATE_API_KEY
import com.helltar.aibot.command.CommandNames.Creator.CMD_UPDATE_CHAT_MODEL
import com.helltar.aibot.command.CommandNames.Creator.CMD_UPDATE_IMAGE_GEN_MODEL
import com.helltar.aibot.command.CommandNames.User.CMD_CHAT
import com.helltar.aibot.command.CommandNames.User.CMD_CHAT_CTX_REMOVE
import com.helltar.aibot.command.CommandNames.User.CMD_IMAGE_GEN

object BotMessages {

    object General {
        fun start() = """
            👋 Welcome to AI Bot!

            How to start:
            • Reply to one of my messages with a question.
            • Use <code>/$CMD_CHAT</code> with your prompt.
            • Use <code>/$CMD_IMAGE_GEN</code> with an image prompt.

            I'm ready when you are.
        """.trimIndent()

        fun about() = """
            <a href="https://github.com/Helltar/artific-intellig-bot">AI Bot on GitHub</a>
            Contact: https://helltar.com
        """.trimIndent()
    }

    object ApiKey {
        const val BAD_LENGTH = "❌ API key is too short"

        fun failAdd(provider: String) =
            "⚠️ Could not add the <b>$provider</b> API key"

        fun failUpdate(provider: String) =
            "⚠️ Could not update the <b>$provider</b> API key"

        fun successAdd(provider: String) =
            "✅ Added the <b>$provider</b> API key"

        fun successUpdate(provider: String) =
            "✅ Updated the <b>$provider</b> API key"
    }

    object Command {
        const val NOT_SUPPORTED_IN_CHAT = "This command is not available in this chat 🙈"
        const val TEMPORARILY_DISABLED = "This command is temporarily disabled 👀"
        const val MANY_REQUEST = "Please wait for your previous request to finish 😊"
        const val ADMIN_ONLY = "Only admins can use this command ✋"
        const val CREATOR_CONTEXT_CANNOT_BE_VIEWED = "You can't view the creator's context ✋"
        const val CREATOR_CONTEXT_CANNOT_BE_DELETED = "You can't delete the creator's context ✋"

        fun alreadyDisabled(commandName: String) =
            "✅ Command <b>$commandName</b> is already disabled"

        fun alreadyEnabled(commandName: String) =
            "✅ Command <b>$commandName</b> is already enabled"

        fun disabled(commandName: String) =
            "✅ Disabled command <b>$commandName</b>"

        fun enabled(commandName: String) =
            "✅ Enabled command <b>$commandName</b>"

        fun notAvailable(commandName: String, availableCommands: String) =
            "Command <b>$commandName</b> is not available. Available commands: $availableCommands"

        fun manyCharacters(maxCharacters: Int) =
            "Max <b>$maxCharacters</b> characters 👀"
    }

    object Chat {
        const val CONTEXT_EMPTY = "▫️ No context yet"
        const val CONTEXT_REMOVED = "Context cleared 👌"
        const val EXCEPTION = "Something went wrong 🫡"
        const val HELLO = "👋 Send <code>/$CMD_CHAT your text</code> or reply to one of my messages to continue the conversation"
        const val MESSAGE_TEXT_NOT_FOUND = "That message has no text 🤔"

        fun savedToFile(subject: String) =
            "Telegram could not send the $subject, so I saved it as a file. Try /$CMD_CHAT_CTX_REMOVE if this keeps happening ☺️"

        fun contextEmpty(userSuffix: String) =
            CONTEXT_EMPTY + userSuffix

        fun contextRemoved(userSuffix: String) =
            CONTEXT_REMOVED + userSuffix

        fun imageMustBeLessThan(maxBytes: Int) =
            "The image must be smaller than ${maxBytes / 1024 / 1024} MB 😥"
    }

    object Moderation {
        const val USER_ALREADY_BANNED = "✅ This user is already banned"
        const val USER_BANNED = "❌ User banned"
        const val USER_NOT_BANNED = "✅ This user is not banned"
        const val USER_UNBANNED = "✅ User unbanned"

        fun banAndReason(reason: String) =
            "❌ You are banned. Reason: <b>$reason</b>"
    }

    object Admins {
        const val ADDED = "✅ Admin added"
        const val EXISTS = "✅ Admin already exists"
        const val REMOVED = "✅ Admin removed"
        const val NOT_EXISTS = "❌ Admin not found"
    }

    object Allowlist {
        const val CHAT_ADDED = "✅ Chat added"
        const val CHAT_EXISTS = "✅ Chat already exists"
        const val CHAT_REMOVED = "✅ Chat removed"
        const val CHAT_NOT_EXISTS = "❌ Chat not found"
    }

    object Slowmode {
        const val CHANGE_FAIL = "❌ Could not update slow mode"

        fun pleaseWait(seconds: Long) =
            "✋ Slow mode is active. Try again in <b>$seconds</b> seconds"

        fun updated(requestsPerHour: Int) =
            "✅ Slow mode is now <b>$requestsPerHour</b> requests per hour per user"
    }

    object Models {
        const val CHAT_FAIL_UPDATE = "⚠️ Could not update the chat model"
        const val IMAGES_FAIL_UPDATE = "⚠️ Could not update the image generation model"
        const val BAD_MODEL_NAME_LENGTH = "❌ Model name is too short"

        fun chatSuccessUpdate(modelName: String) =
            "✅ Chat model updated to <b>$modelName</b>"

        fun imagesSuccessUpdate(modelName: String) =
            "✅ Image generation model updated to <b>$modelName</b>"
    }

    object Ui {
        const val LIST_IS_EMPTY = "◻️ The list is empty"
    }

    object Usage {
        fun updateChatModel(currentModel: String) = """
            ℹ️ Current chat model: <b>$currentModel</b>
            
            To change it, use:
            <code>/$CMD_UPDATE_CHAT_MODEL</code> &lt;chat-model&gt;
        """.trimIndent()

        fun updateImageGenModel(currentModel: String) = """
            ℹ️ Current image generation model: <b>$currentModel</b>
            
            To change it, use:
            <code>/$CMD_UPDATE_IMAGE_GEN_MODEL</code> &lt;image-model&gt;
        """.trimIndent()

        fun slowmode(maxUsageCount: Int) = """
            ℹ️ Current slow mode: <b>$maxUsageCount</b> requests per hour per user.
            
            To change it, use:
            <code>/$CMD_SLOWMODE</code> 15
        """.trimIndent()

        fun updateApiKey() = """
            ℹ️ Usage:
            
            <code>/$CMD_UPDATE_API_KEY</code> sk-proj-qwertyuiop
        """.trimIndent()

        fun imageGen() = """
            ℹ️ Usage:
            
            <code>/$CMD_IMAGE_GEN</code> &lt;image prompt&gt;
        """.trimIndent()
    }
}
