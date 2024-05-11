import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask


plugins {
    kotlin("multiplatform")
    alias(libs.plugins.publish)
}

kotlin {
    jvm()
    js {
        nodejs()
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()

    // these targets are only supported in the core module

}

// https://youtrack.jetbrains.com/issue/KT-63014
// https://github.com/Kotlin/kotlin-wasm-examples/blob/1b007347bf9f8a1ec3d420d30de1815768d5df02/nodejs-example/build.gradle.kts#L22
rootProject.the<NodeJsRootExtension>().apply {
    nodeVersion = "22.0.0-nightly202404032241e8c5b3"
    nodeDownloadBaseUrl = "https://nodejs.org/download/nightly"
}

rootProject.tasks.withType<KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}
