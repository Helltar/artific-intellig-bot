package com.helltar.aibot.openai.models.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentPartData(
    val type: String,
    val text: String? = null,

    @SerialName("image_url")
    val imageUrl: ImageUrlData? = null
)
