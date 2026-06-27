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
    testImplementation(kotlin("test"))
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
}

compose.desktop {
    application {
        mainClass = "com.adferdv.ktile.MainKt"
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
