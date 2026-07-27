package com.helltar.aibot.openai.models.image

import com.helltar.aibot.openai.models.common.ContentPartData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/docs/guides/images-vision/#analyze-images */

@Serializable
data class VisionRequestData(
    val model: String,
    val input: List<VisionMessageData>,
    val instructions: String? = null,

    @SerialName("prompt_cache_key")
    val promptCacheKey: String? = null,

    @SerialName("safety_identifier")
    val safetyIdentifier: String? = null,

    val store: Boolean = false
)

@Serializable
data class VisionMessageData(
    val role: String,
    val content: List<ContentPartData>
)
