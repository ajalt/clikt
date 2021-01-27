plugins {
    id("org.jetbrains.kotlin.plugin.serialization").version("1.3.72")
}

application {
    mainClass.set("com.github.ajalt.clikt.samples.json.MainKt")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}
