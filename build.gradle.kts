import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.1"
    kotlin("plugin.serialization") version "1.9.10"
}

group = "com.dallaslabs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    maven("https://jitpack.io")
}

val ktor_version = "2.3.5"
val koin_version = "3.5.0"
val kotlinx_coroutines_version = "1.7.3"
val kotlinx_serialization_version = "1.6.0"
val logback_version = "1.4.11"

dependencies {
    // Compose
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Koin for dependency injection
    implementation("io.insert-koin:koin-core:$koin_version")
    implementation("io.insert-koin:koin-compose:1.1.0")

    // Ktor client for HTTP network requests
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")

    // KotlinX Serialization for JSON parsing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinx_serialization_version")

    // Coroutines for asynchronous programming
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$kotlinx_coroutines_version")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.github.microutils:kotlin-logging:3.0.5")

    // DateTime handling
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("io.ktor:ktor-serialization-jackson:$ktor_version")

    // Settings/preferences storage
    implementation("com.russhwolf:multiplatform-settings:1.1.0")

    // Window management
    implementation("com.arkivanov.decompose:decompose:2.1.0")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:2.1.0")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinx_coroutines_version")
    testImplementation("io.insert-koin:koin-test:$koin_version")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "OllamaClusterFrontend"
            packageVersion = "1.0.0"

            // Windows specific options
            windows {
                menuGroup = "Ollama Cluster"
                // Defines a shortcut in the start menu
                shortcut = true
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }

            // macOS specific options
            macOS {
                // macOS universal binary support
                //targetArch = org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget.CURRENT
                bundleID = "com.dallaslabs.ollamacluster"
                iconFile.set(project.file("src/main/resources/icon.icns"))
            }

            // Linux specific options
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
