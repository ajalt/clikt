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
