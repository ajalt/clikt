package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.RestoreSystemProperties
import java.nio.file.FileSystem
import java.nio.file.Files
import kotlin.test.Test


@Suppress("unused")
class PathTest {
    @Rule
    @JvmField
    val restoreSystemProperties = RestoreSystemProperties()
    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Test
    fun `paths are resolved using the provided filesystem, if any`() {
        class C : TestCommand() {
            val path by option("-p")
                    .path(fileSystem = fs)
                    .required()

            override fun run_() {
                path.fileSystem shouldBe fs
            }
        }

        C().parse("-p/var/log/foo")
    }

    @Test
    fun `options can be paths`() {
        class C : TestCommand() {
            val path by option("-p")
                    .path(fileSystem = fs)
                    .required()

            override fun run_() {
                path.toString() shouldBe "foo"
            }
        }

        C().parse("-pfoo")
    }

    @Test
    fun `arguments can be paths`() {
        class C : TestCommand() {
            val paths by argument()
                    .path(fileSystem = fs)
                    .multiple()

            override fun run_() {
                paths.map { it.toString() } shouldBe listOf("foo", "bar", "baz")
            }
        }

        C().parse("foo bar baz")
    }

    @Test
    fun `values can be converted before path is called`() {
        class C : TestCommand() {
            val path by option("-p")
                    .convert { "/tmp/$it" }
                    .path(fileSystem = fs)
                    .required()

            override fun run_() {
                path.toString() shouldBe "/tmp/foo"
            }
        }

        C().parse("-pfoo")
    }

    @Test
    fun `canBeFile = false will reject files`() {
        class C : TestCommand() {
            val folderOnly by option("-f").path(canBeFile = false, fileSystem = fs)
        }

        Files.createDirectory(fs.getPath("/var"))
        Files.createFile(fs.getPath("/var/foo"))

        shouldThrow<BadParameterValue> {
            C().parse("-f/var/foo")
        }.message shouldBe """Invalid value for "-f": Directory "/var/foo" is a file."""
    }

    @Test
    fun `canBeDir = false will reject folders`() {
        class C : TestCommand() {
            val fileOnly by option("-f").path(canBeDir = false, fileSystem = fs)
        }

        Files.createDirectories(fs.getPath("/var/foo"))

        shouldThrow<BadParameterValue> {
            C().parse("-f/var/foo")
        }.message shouldBe """Invalid value for "-f": File "/var/foo" is a directory."""
    }

    @Test
    fun `mustExist = true will reject paths that don't exist`() {
        class C : TestCommand() {
            val homeDir by option("-h").path(mustExist = true, fileSystem = fs)
        }

        shouldThrow<BadParameterValue> {
            C().parse("-h /home/cli")
        }.message shouldBe """Invalid value for "-h": Path "/home/cli" does not exist."""
    }
}
