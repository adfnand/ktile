@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

group = "com.adferdv"
version = "1.0.0"

val isLinux = System.getProperty("os.name").lowercase().contains("linux")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(libs.jna.platform)
    implementation(libs.jnativehook)
    implementation(libs.kotlinx.serialization.json)

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

val rustDir = layout.projectDirectory.dir("lib/ktile-hotkey")
val rustReleaseSo = rustDir.file("target/release/libktile_hotkey.so")
val rustLibsDir = layout.buildDirectory.dir("rust-libs")

val buildRustRelease by tasks.registering(Exec::class) {
    group = "rust"
    description = "Build the Rust hotkey library in release mode"
    workingDir = rustDir.asFile
    commandLine("cargo", "build", "--release")
    inputs.dir(rustDir.dir("src"))
    outputs.file(rustReleaseSo)
    onlyIf { isLinux }
}

val copyRustLib by tasks.registering(Copy::class) {
    group = "rust"
    description = "Copy the Rust hotkey shared library to build/rust-libs"
    dependsOn(buildRustRelease)
    from(rustReleaseSo)
    into(rustLibsDir)
    onlyIf { isLinux }
}

tasks.named("processResources") {
    dependsOn(copyRustLib)
}

afterEvaluate {
    tasks.named("prepareAppResources") {
        dependsOn(copyRustLib)
    }
}

compose.desktop {
    application {
        mainClass = "com.adferdv.ktile.MainKt"
        jvmArgs +=
            listOf(
                "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
                "--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED",
                "-Djna.library.path=${rustLibsDir.get().asFile.absolutePath}",
            )
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "ktile"
            packageVersion = version.toString()
            appResourcesRootDir.set(rustLibsDir)
        }
    }
}

detekt {
    config.from("detekt.yml")
    autoCorrect = true
    ignoreFailures = false
}

kover {
    reports {
        filters {
            excludes {
                // App bootstrap and OS-specific integration code: not unit-testable headless,
                // covered by functional tests (core.screen) and per-OS integration tests (providers, window, tray).
                classes(
                    "com.adferdv.ktile.MainKt*",
                    "com.adferdv.ktile.ComposableSingletons*",
                    "com.adferdv.ktile.core.screen.*",
                    "com.adferdv.ktile.core.hotkey.LinuxEvdevHotkeyProvider*",
                    "com.adferdv.ktile.core.hotkey.JNativeHookProvider*",
                    "com.adferdv.ktile.core.hotkey.KtileHotkeyNative*",
                    "com.adferdv.ktile.core.hotkey.InputDevicePermissionChecker",
                    "com.adferdv.ktile.ui.KTileWindowKt*",
                    "com.adferdv.ktile.ui.KTileTrayKt*",
                    "com.adferdv.ktile.ui.GlobalHotkeyRegistration*",
                )
            }
        }
        verify {
            rule {
                minBound(90)
            }
        }
    }
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
        dependsOn(copyRustLib)
        systemProperty("jna.library.path", rustLibsDir.get().asFile.absolutePath)
    }

    named("check") {
        dependsOn("ktlintCheck", "detekt", "test", "koverVerify")
    }
}
