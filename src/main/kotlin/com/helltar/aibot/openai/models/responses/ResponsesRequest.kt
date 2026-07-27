package com.helltar.aibot.openai.models.responses

import com.helltar.aibot.openai.models.common.MessageData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/reference/resources/responses/methods/create */

@Serializable
data class ResponsesRequestData(
    val model: String,
    val input: List<MessageData>,
    val instructions: String? = null,

    @SerialName("prompt_cache_key")
    val promptCacheKey: String? = null,

    @SerialName("safety_identifier")
    val safetyIdentifier: String? = null,

    val store: Boolean = false
)
