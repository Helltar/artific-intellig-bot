package com.helltar.aibot

import io.github.cdimascio.dotenv.dotenv
import java.io.File

object Config {

    private val dotenv = dotenv { ignoreIfMissing = true }

    data class BotConfig(
        val creatorId: Long,
        val telegramBotToken: String,
        val telegramBotUsername: String,
        val openaiApiKey: String,
        val postgresqlHost: String,
        val databaseName: String,
        val databaseUser: String,
        val databasePassword: String
    )

    val botConfig =
        BotConfig(
            creatorId = readEnv("CREATOR_ID").toLongOrNull() ?: throw IllegalArgumentException("invalid CREATOR_ID environment variable"),
            telegramBotToken = readEnv("BOT_TOKEN"),
            telegramBotUsername = readEnv("BOT_USERNAME"),
            openaiApiKey = readEnv("OPENAI_API_KEY"),
            postgresqlHost = readEnv("POSTGRESQL_HOST"),
            databaseName = readEnv("DATABASE_NAME"),
            databaseUser = readEnv("DATABASE_USER"),
            databasePassword = readEnv("DATABASE_PASSWORD")
        )

    val personalityFile =
        readOptionalEnv("PERSONALITY_FILE")?.also { path ->
            require(File(path).canRead()) { "personality file is missing or not readable: $path" }
        }

    private fun readEnv(env: String) =
        dotenv[env]?.ifBlank { throw IllegalArgumentException("environment variable $env is blank") }
            ?: throw IllegalArgumentException("environment variable $env is missing")

    private fun readOptionalEnv(env: String) =
        dotenv[env]?.trim()?.takeIf { it.isNotEmpty() }
}
