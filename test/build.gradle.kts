import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
        commonMain.dependencies {
            api(project(":clikt"))
            api(project(":clikt-mordant"))
            api(project(":clikt-mordant-markdown"))
            api(libs.mordant)
            api(libs.mordant.markdown)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotest)
            implementation(libs.kotest.table)
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.test)
        }

        jvmTest.dependencies {
            implementation(libs.systemrules)
            implementation(libs.jimfs)
        }
    }
}

val jvmTests = tasks.named<Test>("jvmTest")
val javaToolchains = extensions.getByType<JavaToolchainService>()

tasks.register<Test>("jvmCompatibilityTest") {
    description = "Runs JVM tests on Java 11 using the existing compilation toolchain."
    group = "verification"
    testClassesDirs = jvmTests.get().testClassesDirs
    classpath = jvmTests.get().classpath
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
}
