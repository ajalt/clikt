import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    alias(libs.plugins.publish)
}

kotlin {
    jvm()

    js { nodejs() }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { nodejs() }

    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()

    iosArm64()
    iosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":clikt"))
                api(project(":clikt-mordant"))
                api(libs.mordant)
                api(libs.mordant.markdown)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotest)
                implementation(libs.coroutines.core)
                implementation(libs.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.systemrules)
                implementation(libs.jimfs)
            }
        }
    }
}

dokka {
    dokkaSourceSets.configureEach {
        includes.from("README.md")
    }
}
