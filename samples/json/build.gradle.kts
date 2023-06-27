plugins {
    kotlin("plugin.serialization") version libs.versions.kotlin
}

application {
    mainClass.set("com.github.ajalt.clikt.samples.json.MainKt")
}

dependencies {
    implementation(libs.kotlinx.serialization)
}
