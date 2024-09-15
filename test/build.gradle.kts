import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl


plugins {
    kotlin("multiplatform")
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
                api(project(":clikt-mordant-markdown"))
                api(libs.mordant)
                api(libs.mordant.markdown)
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test"))
                api(libs.kotest)
                api(libs.coroutines.core)
                api(libs.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                api(libs.systemrules)
                api(libs.jimfs)
            }
        }
    }
}
