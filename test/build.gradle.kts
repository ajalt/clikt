@file:Suppress("PropertyName")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()
    js("node") { nodejs() }
    js("browser") { browser() }

    linuxX64()
    mingwX64()
    macosX64()

    sourceSets {
        get("commonTest").dependencies {
            api(project(":clikt"))
            api(kotlin("stdlib-common"))
            api(kotlin("test-common"))
            api(kotlin("test-annotations-common"))
            api("io.kotest:kotest-assertions-core:4.1.0")
        }

        get("jvmTest").dependencies {
            api(kotlin("reflect"))
            api(kotlin("test-junit"))
            api("com.github.stefanbirkner:system-rules:1.18.0")
            api("com.google.jimfs:jimfs:1.1")
        }

        val jsTest by creating {
            dependencies {
                api(kotlin("test-js"))
            }
        }

        listOf("nodeTest", "browserTest").forEach {
            get(it).dependsOn(jsTest)
        }

        val nativeTest by creating {}

        listOf("macosX64Test", "linuxX64Test", "mingwX64Test").forEach {
            get(it).dependsOn(nativeTest)
        }
    }
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}
