package com.helltar.aibot

import io.github.cdimascio.dotenv.dotenv

object Config {

    const val LOADING_ANIMATION_FILE = "data/files/loading.gif"
    const val SYSTEM_PROMPT_FILE = "data/files/system.prompt"

    private val dotenv = dotenv { ignoreIfMissing = true }

    val creatorId = readEnvVar("CREATOR_ID").toLongOrNull() ?: throw IllegalArgumentException("invalid CREATOR_ID environment variable")
    val telegramBotToken = readEnvVar("BOT_TOKEN")
    val telegramBotUsername = readEnvVar("BOT_USERNAME")
    val postgresqlHost = readEnvVar("POSTGRESQL_HOST")
    val postgresqlPort = readEnvVar("POSTGRESQL_PORT")
    val databaseName = readEnvVar("DATABASE_NAME")
    val databaseUser = readEnvVar("DATABASE_USER")
    val databasePassword = readEnvVar("DATABASE_PASSWORD")

    private fun readEnvVar(env: String) =
        dotenv[env].ifBlank { throw IllegalArgumentException("environment variable $env is blank") }
            ?: throw IllegalArgumentException("environment variable $env is missing")
}
