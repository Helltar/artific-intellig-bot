plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

group = "com.helltar"
version = "0.17.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.tgbots.module) { exclude("org.telegram", "telegrambots-webhook") }
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.exposed)
    runtimeOnly(libs.r2dbc.postgresql)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.dotenv.kotlin)
    implementation(libs.kotlin.logging.jvm)
    runtimeOnly(libs.logback.classic)
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.helltar.aibot.MainKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}
