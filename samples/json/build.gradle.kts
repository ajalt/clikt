buildscript {
    repositories {
        jcenter()
    }
}

plugins {
    id("org.jetbrains.kotlin.plugin.serialization").version("1.3.72")
}

application {
    mainClassName = "com.github.ajalt.clikt.samples.json.MainKt"
}

repositories {
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}
