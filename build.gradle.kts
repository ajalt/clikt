import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile


plugins {
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    alias(libs.plugins.publish).apply(false)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kotlinBinaryCompatibilityValidator)
}

apiValidation {
    // https://github.com/Kotlin/binary-compatibility-validator/issues/3
    project("samples").subprojects.mapTo(ignoredProjects) { it.name }
}

fun getPublishVersion(): String {
    val version = project.property("VERSION_NAME").toString()
    // Call gradle with -PsnapshotVersion to set the version as a snapshot.
    if (!project.hasProperty("snapshotVersion")) return version
    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "0"
    return "$version.$buildNumber-SNAPSHOT"
}


private val dokkaConfig = mapOf(
    "org.jetbrains.dokka.base.DokkaBase" to """{
        "footerMessage": "Copyright &copy; 2018 AJ Alt"
    }"""
)

subprojects {
    project.setProperty("VERSION_NAME", getPublishVersion())

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(8)
    }
    pluginManager.withPlugin("com.vanniktech.maven.publish") {
        apply(plugin = "org.jetbrains.dokka")
    }
    tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            reportUndocumented.set(false)
            skipDeprecated.set(false)
        }
    }
    tasks.withType<DokkaTaskPartial>().configureEach {
        pluginsMapConfiguration.set(dokkaConfig)
    }
}

tasks.named<DokkaMultiModuleTask>("dokkaHtmlMultiModule") {
    outputDirectory.set(rootProject.rootDir.resolve("docs/api"))
    pluginsMapConfiguration.set(dokkaConfig)
}
