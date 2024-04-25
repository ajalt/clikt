@file:Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress")

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

    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.mordant)
            }
        }

        val commonTest by getting {
            dependencies {
                api(kotlin("test"))
                api(libs.kotest)
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
