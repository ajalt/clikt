include("clikt")
include("samples:copy")
include("samples:repo")
include("samples:validation")
include("samples:aliases")
include("samples:helpformat")
include("samples:ansicolors")
include("samples:plugins")
include("samples:json")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.7.21")

            plugin("dokka", "org.jetbrains.dokka").version("1.7.20")

            // used in tests
            library("dokka-base", "org.jetbrains.dokka:dokka-base:1.7.20")
            library("kotest", "io.kotest:kotest-assertions-core:5.2.1")
            library("systemrules", "com.github.stefanbirkner:system-rules:1.19.0")
            library("jimfs", "com.google.jimfs:jimfs:1.1")

            // used in samples
            library("kodein", "org.kodein.di:kodein-di-generic-jvm:5.2.0")
            library("kotlinx-serialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            library("mordant", "com.github.ajalt:mordant:1.2.1")
        }
    }
}
