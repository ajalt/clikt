@file:Suppress("PropertyName")

import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}


// True if intellij is running. When true, we create a single native target named "native" using the
// current OS. Otherwise (when run with gradle), we create all native targets and have them depend
// on the native module. If we don't do this, IntelliJ won't know what target the nativeMain source
// set is for, and won't resolve references to native libraries.
val ideaActive = System.getProperty("idea.active") == "true"
val os = org.gradle.internal.os.OperatingSystem.current()!!

kotlin {
    jvm()
    js { nodejs() }

    linuxX64()
    mingwX64()
    macosX64()

    if (ideaActive) {
        if (os.isMacOsX) macosX64("native")
        if (os.isWindows) mingwX64("native")
        if (os.isLinux) linuxX64("native")
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }

        get("commonMain").dependencies {
            api(kotlin("stdlib-common"))
        }

        get("commonTest").dependencies {
            api(kotlin("test-common"))
            api(kotlin("test-annotations-common"))
            api("io.kotest:kotest-assertions-core:4.1.0")
        }

        get("jvmMain").dependencies {
            api(kotlin("stdlib"))
        }

        get("jvmTest").dependencies {
            api(kotlin("reflect"))
            api(kotlin("test-junit"))
            api("com.github.stefanbirkner:system-rules:1.18.0")
            api("com.google.jimfs:jimfs:1.1")
        }

        get("jsMain").dependencies {
            api(kotlin("stdlib-js"))
        }

        get("jsTest").dependencies {
            api(kotlin("test-js"))
        }

        val nativeMain = if (ideaActive) get("nativeMain") else create("nativeMain")

        listOf("macosX64Main", "linuxX64Main", "mingwX64Main").forEach {
            get(it).dependsOn(nativeMain)
        }

        val nativeTest = if (ideaActive) get("nativeTest") else create("nativeTest")

        listOf("macosX64Test", "linuxX64Test", "mingwX64Test").forEach {
            get(it).dependsOn(nativeTest)
        }
    }
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

val dokka by tasks.getting(DokkaTask::class) {
    outputDirectory = "$rootDir/docs/api"
    outputFormat = "gfm"
    multiplatform {}
}

val dokkaPostProcess by tasks.registering(DokkaProcess::class) {
    inputs.files(dokka.outputs.files)
}

val emptyJavadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
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

    publications {
        // Keep the old publication name for the JVM target
        (getByName("jvm") as MavenPublication).artifactId = "clikt"
        (getByName("kotlinMultiplatform") as MavenPublication).artifactId = "clikt-multiplatform"
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
