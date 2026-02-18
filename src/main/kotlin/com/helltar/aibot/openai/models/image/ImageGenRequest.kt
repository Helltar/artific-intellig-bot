package com.helltar.aibot.openai.models.image

import kotlinx.serialization.Serializable

/* https://developers.openai.com/api/reference/resources/images/methods/generate */

@Serializable
data class ImageGenRequest(
    val model: String,
    val prompt: String,
    val n: Int,
    val size: String
)
