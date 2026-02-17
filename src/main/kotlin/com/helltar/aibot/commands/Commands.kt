package com.helltar.aibot.commands

import com.helltar.aibot.commands.Commands.User.CMD_CHAT
import com.helltar.aibot.commands.Commands.User.CMD_IMAGE_GEN

object Commands {

    object General {
        const val CMD_ABOUT = "about"
        const val CMD_MYID = "myid"
        const val CMD_START = "start"
    }

    object User {
        const val CMD_CHAT = "chat"
        const val CMD_CHATCTX = "chatctx" // dialog context
        const val CMD_CHAT_CTX_REMOVE = "chatrm" // remove context
        const val CMD_IMAGE_GEN = "imgen"
    }

    object Creator {
        const val CMD_ADD_ADMIN = "addadmin"
        const val CMD_ADD_CHAT = "addchat"
        const val CMD_SLOWMODE = "slowmode"
        const val CMD_UPDATE_API_KEY = "updatekey"
        const val CMD_UPDATE_CHAT_MODEL = "chatmodel"
        const val CMD_UPDATE_IMAGE_GEN_MODEL = "imgmodel"
    }

    object Admin {
        const val CMD_ADMIN_LIST = "sudoers"
        const val CMD_BAN_LIST = "banlist"
        const val CMD_BAN_USER = "ban"
        const val CMD_CHAT_ALLOW_LIST = "chats"
        const val CMD_DISABLE = "disable"
        const val CMD_ENABLE = "enable"
        const val CMD_RM_ADMIN = "rmadmin"
        const val CMD_RM_CHAT = "rmchat"
        const val CMD_UNBAN_USER = "unban"
    }

    val disableableCommands =
        setOf(CMD_CHAT, CMD_IMAGE_GEN)
}
