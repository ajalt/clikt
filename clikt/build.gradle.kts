@file:Suppress("PropertyName", "UNUSED_VARIABLE")

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


@Suppress("DSL_SCOPE_VIOLATION", "UnstableApiUsage") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    kotlin("multiplatform")
    alias(libs.plugins.dokka)
    id("maven-publish")
    id("signing")
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

// Disable npm install scripts
tasks.withType<KotlinNpmInstallTask> {
    args += "--ignore-scripts"
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

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}


tasks.dokkaHtml.configure {
    outputDirectory.set(rootDir.resolve("docs/api"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customStyleSheets = listOf(rootDir.resolve("docs/css/logo-styles.css"))
        customAssets = listOf(rootDir.resolve("docs/img/wordmark_small_dark.svg"))
        footerMessage = "Copyright &copy; 2022 AJ Alt"
    }
    dokkaSourceSets {
        configureEach {
            reportUndocumented.set(false)
            skipDeprecated.set(true)
        }
    }
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

val jvmJar by tasks.getting(Jar::class) {
    manifest {
        attributes("Automatic-Module-Name" to "com.github.ajalt.clikt")
    }
}

val isSnapshot = version.toString().endsWith("SNAPSHOT")
val signingKey: String? by project
val SONATYPE_USERNAME: String? by project
val SONATYPE_PASSWORD: String? by project

publishing {
    publications.withType<MavenPublication>().all {
        pom {
            description.set("Multiplatform command line interface parsing for Kotlin")
            name.set("Clikt")
            url.set("https://github.com/ajalt/clikt")
            scm {
                url.set("https://github.com/ajalt/clikt")
                connection.set("scm:git:git://github.com/ajalt/clikt.git")
                developerConnection.set("scm:git:ssh://git@github.com/ajalt/clikt.git")
            }
            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("ajalt")
                    name.set("AJ Alt")
                    url.set("https://github.com/ajalt")
                }
            }
        }
    }

    repositories {
        val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
        maven {
            url = if (isSnapshot) snapshotUrl else releaseUrl
            credentials {
                username = SONATYPE_USERNAME ?: ""
                password = SONATYPE_PASSWORD ?: ""
            }
        }
    }

    publications.withType<MavenPublication>().all {
        artifact(emptyJavadocJar.get())
    }
}

signing {
    isRequired = !isSnapshot

    if (signingKey != null && !isSnapshot) {
        @Suppress("UnstableApiUsage")
        useInMemoryPgpKeys(signingKey, "")
        sign(publishing.publications)
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
