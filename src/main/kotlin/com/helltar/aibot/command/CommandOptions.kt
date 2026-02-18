package com.helltar.aibot.command

data class CommandOptions(
    val checkRights: Boolean,
    val isAdminCommand: Boolean,
    val isCreatorCommand: Boolean,
    val isLongRunningCommand: Boolean,
    val privateChatOnly: Boolean
)
