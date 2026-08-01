@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

group = "com.adferdv"
version = "1.0.0"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.jna.platform)

    // Kotest (unit tests)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)

    // ─── Compose UI Tests ───
    // Desktop runtime for tests
    testImplementation(compose.desktop.currentOs)

    // Core UI test API (provides `androidx.compose.ui.test` package)
    testImplementation(compose.uiTest)

    // JUnit4 integration (provides `createComposeRule`)
    testImplementation(compose.desktop.uiTestJUnit4)

    // JUnit4 runtime (for @Test, @Rule)
    testImplementation("junit:junit:4.13.2")

    // Allows JUnit4 tests to run on the JUnit5 platform
    testImplementation("org.junit.vintage:junit-vintage-engine:5.10.1")
}

compose.desktop {
    application {
        mainClass = "com.adferdv.ktile.MainKt"
        jvmArgs +=
            listOf(
                "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED",
            )
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "ktile"
            packageVersion = version.toString()
        }
    }
}

detekt {
    config.from("detekt.yml")
    autoCorrect = true
    ignoreFailures = false
}

ktlint {
    filter {
        exclude { element -> element.file.path.contains("generated/") }
    }
    outputToConsole.set(true)
    ignoreFailures.set(false)
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging.events("passed", "skipped", "failed", "standardOut", "standardError")
        outputs.upToDateWhen { false }
        ignoreFailures = false
    }

    named("check") {
        dependsOn("ktlintCheck", "detekt", "test", "koverVerify")
    }
}
