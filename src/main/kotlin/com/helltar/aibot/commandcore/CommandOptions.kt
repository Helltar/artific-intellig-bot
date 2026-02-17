package com.helltar.aibot.commandcore

data class CommandOptions(
    val checkRights: Boolean,
    val isAdminCommand: Boolean,
    val isCreatorCommand: Boolean,
    val isLongRunningCommand: Boolean,
    val privateChatOnly: Boolean
)
