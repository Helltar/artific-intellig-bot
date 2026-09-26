plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    application
}

group = "com.helltar"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.tgbots.module) { exclude("org.telegram", "telegrambots-webhook") }
    implementation(libs.heartbeat)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.exposed)
    runtimeOnly(libs.r2dbc.postgresql)
    runtimeOnly(libs.r2dbc.pool)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.dotenv.kotlin)
    implementation(libs.kotlin.logging.jvm)
    runtimeOnly(libs.logback.classic)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.mock)
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

tasks.shadowJar {
    // r2dbc finds its drivers through META-INF/services, and the pool and postgresql both ship the same file:
    // it has to be merged, while the default EXCLUDE strategy would drop the second one before any merging
    mergeServiceFiles()
    filesMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
