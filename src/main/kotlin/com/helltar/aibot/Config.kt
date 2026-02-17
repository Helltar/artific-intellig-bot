package com.helltar.aibot

import io.github.cdimascio.dotenv.dotenv

object Config {

    const val LOADING_ANIMATION_FILE = "data/files/loading.gif"
    const val SYSTEM_PROMPT_FILE = "data/files/system.prompt"

    private val dotenv = dotenv { ignoreIfMissing = true }

    data class BotConfig(
        val creatorId: Long,
        val telegramBotToken: String,
        val telegramBotUsername: String,
        val postgresqlHost: String,
        val postgresqlPort: Int,
        val databaseName: String,
        val databaseUser: String,
        val databasePassword: String
    )

    val botConfig =
        BotConfig(
            creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw IllegalArgumentException("invalid CREATOR_ID environment variable"),
            telegramBotToken = readEnv("BOT_TOKEN"),
            telegramBotUsername = readEnv("BOT_USERNAME"),
            postgresqlHost = readEnv("POSTGRESQL_HOST"),
            postgresqlPort = readEnv("POSTGRESQL_PORT").toIntOrNull() ?: 5432,
            databaseName = readEnv("DATABASE_NAME"),
            databaseUser = readEnv("DATABASE_USER"),
            databasePassword = readEnv("DATABASE_PASSWORD")
        )

    private fun readEnv(env: String) =
        dotenv[env].ifBlank { throw IllegalArgumentException("environment variable $env is blank") }
            ?: throw IllegalArgumentException("environment variable $env is missing")
}
