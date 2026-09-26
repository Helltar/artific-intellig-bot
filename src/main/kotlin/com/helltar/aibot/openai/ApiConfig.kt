package com.helltar.aibot.openai

object ApiConfig {

    const val BASE_URL = "https://api.openai.com/v1"

    object Endpoints {
        const val RESPONSES = "/responses"
        const val IMAGES_GENERATIONS = "/images/generations"
    }

    object ChatRole {
        const val USER = "user"
        const val ASSISTANT = "assistant"
        const val SYSTEM = "system"
    }

    object InputContentType {
        const val TEXT = "input_text"
        const val IMAGE = "input_image"
    }

    object OutputType {
        const val MESSAGE = "message"
        const val TEXT = "output_text"
    }

    /* https://developers.openai.com/api/docs/guides/reasoning#reasoning-effort */
    object ReasoningEffort {
        // not an api value: the reasoning field is omitted and the model uses its own default
        const val MODEL_DEFAULT = "default"

        // supported values are model-dependent, an unsupported one is rejected by the api with 400
        val VALUES = listOf("none", "minimal", "low", "medium", "high", "xhigh", "max")
    }
}
