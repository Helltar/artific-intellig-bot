package com.helltar.aibot.command.admin.settings

import com.helltar.aibot.command.BotCommandContext
import com.helltar.aibot.command.CommandNames
import com.helltar.aibot.command.base.BotCommand
import com.helltar.aibot.database.dao.configurationsDao
import com.helltar.aibot.messages.BotMessages
import com.helltar.aibot.openai.ApiConfig.ReasoningEffort

class UpdateReasoningEffort(ctx: BotCommandContext) : BotCommand(ctx) {

    override suspend fun run() {
        if (arguments.isEmpty()) {
            val effort = configurationsDao.reasoningEffort()
            replyToMessage(BotMessages.Usage.updateReasoningEffort(effort))
            return
        }

        val effort = arguments[0].trim().lowercase()

        if (effort != ReasoningEffort.MODEL_DEFAULT && effort !in ReasoningEffort.VALUES) {
            replyToMessage(BotMessages.Models.BAD_REASONING_EFFORT)
            return
        }

        if (configurationsDao.updateReasoningEffort(effort))
            replyToMessage(BotMessages.Models.reasoningEffortSuccessUpdate(effort))
        else
            replyToMessage(BotMessages.Models.REASONING_EFFORT_FAIL_UPDATE)
    }

    override fun commandName() =
        CommandNames.Creator.CMD_UPDATE_REASONING_EFFORT
}
