package com.helltar.aibot.openai.models.chat

import com.helltar.aibot.openai.models.common.MessageData
import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/reference/resources/chat/subresources/completions/methods/create */

@Serializable
data class ChatRequestData(
    val model: String,
    val messages: List<MessageData>
)
