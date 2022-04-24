include("clikt")
include("samples:copy")
include("samples:repo")
include("samples:validation")
include("samples:aliases")
include("samples:helpformat")
include("samples:ansicolors")
include("samples:plugins")
include("samples:json")


@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.10")

            plugin("dokka", "org.jetbrains.dokka").version("1.7.20")
            library("dokka-base", "org.jetbrains.dokka:dokka-base:1.7.20")

            // TODO: switch to published mordant
            library("mordant", "com.github.ajalt.mordant:mordant:2.0.0-local")

            // used in tests
            library("kotest", "io.kotest:kotest-assertions-core:5.5.4")
            library("systemrules", "com.github.stefanbirkner:system-rules:1.19.0")
            library("jimfs", "com.google.jimfs:jimfs:1.1")

            // used in samples
            library("kodein", "org.kodein.di:kodein-di-generic-jvm:5.2.0")
            library("kotlinx-serialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
        }
    }
}
