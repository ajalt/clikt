import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.publish)
}

kotlin {
    jvm()

    js { nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi { nodejs() }

    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()

    iosArm64()
    iosX64()
    iosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
}

// https://youtrack.jetbrains.com/issue/KT-63014
// https://github.com/Kotlin/kotlin-wasm-examples/blob/1b007347bf9f8a1ec3d420d30de1815768d5df02/nodejs-example/build.gradle.kts#L22
rootProject.plugins.withType<NodeJsPlugin> {
    rootProject.the<NodeJsEnvSpec>().apply {
        version.set("22.0.0-nightly202404032241e8c5b3")
        downloadBaseUrl.set("https://nodejs.org/download/nightly")
    }
}

rootProject.tasks.withType<KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}

dokka {
    moduleName.set("clikt-core")
    dokkaSourceSets.configureEach {
        includes.from("README.md")
    }
}
