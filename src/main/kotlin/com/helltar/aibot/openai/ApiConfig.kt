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
}
