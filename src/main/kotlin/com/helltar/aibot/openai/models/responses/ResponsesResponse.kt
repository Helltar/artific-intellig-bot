package com.helltar.aibot.openai.models.responses

import com.helltar.aibot.openai.ApiConfig.OutputType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/reference/resources/responses/methods/create#returns */

@Serializable
data class ResponsesResponseData(
    val model: String,
    val output: List<OutputItemData>,
    val status: String? = null,

    @SerialName("incomplete_details")
    val incompleteDetails: IncompleteDetailsData? = null,

    val usage: UsageData? = null
) {

    fun outputText(): String =
        outputContent(OutputType.TEXT).joinToString("") { it.text.orEmpty() }

    /**
     * The text to show to the user: the output text, or the refusal when the model declined to answer.
     *
     * Fails when there is neither, for example when an incomplete response stopped before any visible
     * output, so an empty reply is never passed on.
     */
    fun answerText(): String =
        outputText()
            .ifBlank { outputContent(OutputType.REFUSAL).joinToString("") { it.refusal.orEmpty() } }
            .ifBlank { error("response has no text: status=$status, reason=${incompleteDetails?.reason}") }

    private fun outputContent(type: String): List<OutputContentData> =
        output
            .filter { it.type == OutputType.MESSAGE }
            .flatMap { it.content.orEmpty() }
            .filter { it.type == type }
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
    val text: String? = null,
    val refusal: String? = null
)

@Serializable
data class IncompleteDetailsData(
    val reason: String? = null
)

@Serializable
data class UsageData(

    @SerialName("input_tokens")
    val inputTokens: Int,

    @SerialName("input_tokens_details")
    val inputTokensDetails: InputTokensDetailsData? = null,

    @SerialName("output_tokens")
    val outputTokens: Int,

    @SerialName("output_tokens_details")
    val outputTokensDetails: OutputTokensDetailsData? = null,

    @SerialName("total_tokens")
    val totalTokens: Int
) {

    // one log line per request, to see how much of the prompt is served from the cache
    fun summary(): String =
        "input=$inputTokens (cached=${inputTokensDetails?.cachedTokens ?: 0}, cache_write=${inputTokensDetails?.cacheWriteTokens ?: 0}), " +
                "output=$outputTokens (reasoning=${outputTokensDetails?.reasoningTokens ?: 0})"
}

@Serializable
data class InputTokensDetailsData(

    @SerialName("cached_tokens")
    val cachedTokens: Int = 0,

    // reported by the models that bill cache writes separately (gpt-5.6 and later)
    @SerialName("cache_write_tokens")
    val cacheWriteTokens: Int = 0
)

@Serializable
data class OutputTokensDetailsData(

    @SerialName("reasoning_tokens")
    val reasoningTokens: Int = 0
)
