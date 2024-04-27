import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile


plugins {
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    alias(libs.plugins.dokka).apply(false)
    alias(libs.plugins.publish).apply(false)
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

        tasks.named<DokkaTask>("dokkaHtml") {
            outputDirectory.set(rootProject.rootDir.resolve("docs/api"))
            val rootPath = rootProject.rootDir.toPath()
            val logoCss = rootPath.resolve("docs/css/logo-styles.css").toString().replace('\\', '/')
            val paletteSvg = rootPath.resolve("docs/img/wordmark_small_dark.svg").toString()
                .replace('\\', '/')
            pluginsMapConfiguration.set(
                mapOf(
                    "org.jetbrains.dokka.base.DokkaBase" to """{
                "customStyleSheets": ["$logoCss"],
                "customAssets": ["$paletteSvg"],
                "footerMessage": "Copyright &copy; 2021 AJ Alt"
            }"""
                )
            )
            dokkaSourceSets.configureEach {
                reportUndocumented.set(false)
                skipDeprecated.set(true)
            }
        }
    }
}
