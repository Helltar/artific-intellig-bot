package com.helltar.aibot.openai.models.image

import com.helltar.aibot.openai.models.common.ContentPartData
import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/docs/guides/images-vision/#analyze-images */

@Serializable
data class VisionRequestData(
    val model: String,
    val messages: List<VisionMessageData>
)

@Serializable
data class VisionMessageData(
    val role: String,
    val content: List<ContentPartData>
)
