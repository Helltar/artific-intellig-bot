plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
    application
}

group = "com.helltar"
version = "0.9.700"

repositories {
    mavenCentral()
}

object Versions {
    const val TGBOTS_MODULE = "9.3.0"
    const val JACKSON_MODULE_KOTLIN = "2.14.2"
    const val DOTENV_KOTLIN = "6.4.1"
    const val EXPOSED = "1.0.0"
    const val R2DBC_POSTGRESQL = "1.1.0.RELEASE"
    const val KTOR = "3.4.0"
    const val KOTLIN_LOGGING = "7.0.13"
    const val LOGBACK_CLASSIC = "1.5.21"
}

dependencies {
    implementation("com.annimon:tgbots-module:${Versions.TGBOTS_MODULE}") { exclude("org.telegram", "telegrambots-webhook") }

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.JACKSON_MODULE_KOTLIN}")
    implementation("io.github.cdimascio:dotenv-kotlin:${Versions.DOTENV_KOTLIN}")

    runtimeOnly("org.postgresql:r2dbc-postgresql:${Versions.R2DBC_POSTGRESQL}")
    implementation("org.jetbrains.exposed:exposed-r2dbc:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")

    implementation("io.ktor:ktor-client-cio:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-auth:${Versions.KTOR}")
    implementation("io.ktor:ktor-client-content-negotiation:${Versions.KTOR}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.KTOR}")

    implementation("io.github.oshai:kotlin-logging-jvm:${Versions.KOTLIN_LOGGING}")
    runtimeOnly("ch.qos.logback:logback-classic:${Versions.LOGBACK_CLASSIC}")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.helltar.aibot.bot.ArtificIntelligBot")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
