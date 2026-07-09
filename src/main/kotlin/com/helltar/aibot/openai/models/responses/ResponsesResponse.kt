package com.helltar.aibot.openai.models.responses

import com.helltar.aibot.openai.ApiConfig.OutputType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponsesResponseData(
    val model: String,
    val output: List<OutputItemData>,
    val usage: UsageData? = null
) {

    fun outputText(): String =
        output
            .filter { it.type == OutputType.MESSAGE }
            .flatMap { it.content.orEmpty() }
            .filter { it.type == OutputType.TEXT }
            .joinToString("") { it.text.orEmpty() }
}

@Serializable
data class OutputItemData(
    val type: String,
    val role: String? = null,
    val content: List<OutputContentData>? = null
)

@Serializable
data class OutputContentData(
    val type: String,
    val text: String? = null
)

@Serializable
data class UsageData(

    @SerialName("input_tokens")
    val inputTokens: Int,

    @SerialName("output_tokens")
    val outputTokens: Int,

    @SerialName("total_tokens")
    val totalTokens: Int
)
