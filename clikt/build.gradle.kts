@file:Suppress("UNUSED_VARIABLE", "KotlinRedundantDiagnosticSuppress")


plugins {
    kotlin("multiplatform")
    alias(libs.plugins.publish)
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    linuxX64()
    mingwX64()
    macosX64()
    macosArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }

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
        val macosX64Main by getting {
            dependsOn(nativeDarwinMain)
        }
        val macosArm64Main by getting {
            dependsOn(nativeDarwinMain)
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
        val macosX64Test by getting {
            dependsOn(nativeDarwinTest)
        }
        val macosArm64Test by getting {
            dependsOn(nativeDarwinTest)
        }
    }
}



/**
 * Replace some ugly Dokka markdown with explicit html tags
 */
open class DokkaProcess : DefaultTask() {
    @TaskAction
    fun processFiles() {
        // The dokka output is the outputDirectory, not each individual file
        val first = inputs.files.files.first()
        val files = first.walkTopDown().filter { it.isFile && it.extension == "md" }
        for (file in files) {
            val text = file.readLines().map {
                if (it.startsWith("`") && (it.endsWith("`") || it.endsWith(")"))) {
                    "<code>" + it.replace("`<`", "&lt;").replace("`>`", "&gt;").replace("`", "") + "</code>"
                } else it
            }
            file.writeText(text.joinToString("\n"))
        }
    }
}
