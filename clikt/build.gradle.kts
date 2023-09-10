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
    mingwX64()

    val darwinTargets = listOf(
        macosArm64(),
        macosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        iosX64(),
        watchosArm64(),
        watchosSimulatorArm64(),
        watchosX64(),
        tvosArm64(),
        tvosSimulatorArm64(),
        tvosX64()
    )

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

        val nativeMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }
        val mingwX64Main by getting {
            dependsOn(nativeMain)
        }
        val nativeDarwinMain by creating {
            dependsOn(nativeMain)
        }
        darwinTargets.forEach {
            getByName("${it.targetName}Main") {
                dependsOn(nativeDarwinMain)
            }
        }

        val nativeTest by creating {
            dependsOn(commonTest)
        }
        val linuxX64Test by getting {
            dependsOn(nativeTest)
        }
        val mingwX64Test by getting {
            dependsOn(nativeTest)
        }
        val nativeDarwinTest by creating {
            dependsOn(nativeTest)
        }
        darwinTargets.forEach {
            getByName("${it.targetName}Test") {
                dependsOn(nativeDarwinTest)
            }
        }
    }
}
