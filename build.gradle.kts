import java.io.ByteArrayOutputStream

val VERSION_NAME: String by project

plugins {
    kotlin("jvm").version("1.3.71")
    id("org.jetbrains.dokka").version("0.10.1")
}

allprojects {
    group = "com.github.ajalt"
    version = getPublishVersion()

    repositories {
        jcenter()
    }
}

fun getPublishVersion(): String {
    // Call gradle with -PinferVersion to set the dynamic version name. Otherwise we skip it to save time.
    if (!project.hasProperty("inferVersion")) return VERSION_NAME

    val stdout = ByteArrayOutputStream()
    project.exec {
        commandLine = listOf("git", "tag", "--points-at", "master")
        standardOutput = stdout
    }
    val tag = String(stdout.toByteArray()).trim()
    if (tag.isNotEmpty()) return tag

    stdout.reset()
    project.exec {
        commandLine = listOf("git", "rev-list", "--count", "master")
        standardOutput = stdout
    }
    val buildNumber = String(stdout.toByteArray()).trim()
    if (buildNumber.isNotEmpty()) return "$VERSION_NAME.$buildNumber-SNAPSHOT"

    return VERSION_NAME
}
