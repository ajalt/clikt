plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "1.4.10"
}

application {
    mainClass.set("com.github.ajalt.clikt.samples.json.MainKt")
}

dependencies {
    implementation(libs.kotlinx.serialization)
}
