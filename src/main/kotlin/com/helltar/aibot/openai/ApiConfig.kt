package com.helltar.aibot.openai

object ApiConfig {

    const val PROVIDER_NAME = "openai.com"
    const val BASE_URL = "https://api.openai.com/v1"

    object Endpoints {
        const val CHAT_COMPLETIONS = "/chat/completions"
    }

    object ChatRole {
        const val USER = "user"
        const val ASSISTANT = "assistant"
        const val SYSTEM = "system"
    }

    object ChatContentType {
        const val TEXT = "text"
        const val IMAGE_URL = "image_url"
    }
}
