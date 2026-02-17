package com.helltar.aibot.openai.models.image

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ImageGenResponseData(
    val data: List<ImageGenImageData>
)

@Serializable
data class ImageGenImageData(
    @SerialName("b64_json")
    val b64Json: String? = null
)
