import org.jetbrains.dokka.gradle.DokkaTaskPartial
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
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }
}

// https://youtrack.jetbrains.com/issue/KT-63014
// https://github.com/Kotlin/kotlin-wasm-examples/blob/1b007347bf9f8a1ec3d420d30de1815768d5df02/nodejs-example/build.gradle.kts#L22
rootProject.the<NodeJsRootExtension>().apply {
    version = "22.0.0-nightly202404032241e8c5b3"
    downloadBaseUrl = "https://nodejs.org/download/nightly"
}

rootProject.tasks.withType<KotlinNpmInstallTask>().configureEach {
    args.add("--ignore-engines")
}

tasks.withType<DokkaTaskPartial> {
    dokkaSourceSets.configureEach {
        moduleName.set("clikt-core")
        includes.from("README.md")
    }
}
