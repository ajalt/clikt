import java.io.ByteArrayOutputStream

val VERSION_NAME: String by project

plugins {
    kotlin("jvm").version("1.5.0")
}

allprojects {
    group = "com.github.ajalt.clikt"
    version = getPublishVersion()

    repositories {
        mavenCentral()
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

    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
    return "$VERSION_NAME.$buildNumber-SNAPSHOT"
}
