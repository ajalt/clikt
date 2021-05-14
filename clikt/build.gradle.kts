@file:Suppress("PropertyName", "UNUSED_VARIABLE")

import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka") version "1.4.32"
    id("maven-publish")
    id("signing")
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.4.32")
    }
}


kotlin {
    jvm()

    /*
     * We would like to use
     *     js(BOTH)
     * to enable consumers to use IR JS backend
     * https://kotlinlang.org/docs/js-ir-compiler.html
     * However this is currently blocked by https://youtrack.jetbrains.com/issue/KT-43490
     */
    js {
        nodejs()
        browser()
    }

    linuxX64()
    mingwX64()
    macosX64()

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        val commonMain by getting {}

        val commonTest by getting {
            dependencies {
                api(kotlin("test"))
                api("io.kotest:kotest-assertions-core:4.5.0")
            }
        }

        val jvmTest by getting {
            dependencies {
                api("com.github.stefanbirkner:system-rules:1.18.0")
                api("com.google.jimfs:jimfs:1.1")
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
        val macosX64Main by getting {
            dependsOn(nativeMain)
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
        val macosX64Test by getting {
            dependsOn(nativeTest)
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
        footerMessage = "Copyright &copy; 2021 AJ Alt"
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
