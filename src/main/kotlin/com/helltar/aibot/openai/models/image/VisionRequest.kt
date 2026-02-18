package com.helltar.aibot.openai.models.image

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/docs/guides/images-vision/ */

@Serializable
data class VisionRequestData(
    val model: String,
    val messages: List<VisionMessageData>
)

@Serializable
data class VisionMessageData(
    val role: String,
    val content: List<VisionContentData>
)

@Serializable
data class VisionContentData(
    val type: String,
    val text: String? = null,

    @SerialName("image_url")
    val imageUrl: VisionImageData? = null
)

@Serializable
data class VisionImageData(
    val url: String
)
