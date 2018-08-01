package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.testing.splitArgv
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import java.nio.file.FileSystem
import java.nio.file.Files

@Suppress("unused")
class PathTest {
    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Test
    fun `paths are resolved using the provided filesystem, if any`() {
        class C : CliktCommand() {
            val path by option("-p")
                    .path(fileSystem = fs)
                    .required()

            override fun run() {
                path.fileSystem shouldBe fs
            }
        }

        C().parse(splitArgv("-p/var/log/foo"))
    }

    @Test
    fun `options can be paths`() {
        class C : CliktCommand() {
            val path by option("-p")
                    .path()
                    .required()

            override fun run() {
                path.toString() shouldBe "foo"
            }
        }

        C().parse(splitArgv("-pfoo"))
    }

    @Test
    fun `arguments can be paths`() {
        class C : CliktCommand() {
            val paths by argument()
                    .path()
                    .multiple()

            override fun run() {
                paths.map { it.toString() } shouldBe listOf("foo", "bar", "baz")
            }
        }

        C().parse(splitArgv("foo bar baz"))
    }

    @Test
    fun `fileOkay = false will reject files`() {
        class C : NoRunCliktCommand() {
            val folderOnly by option("-f").path(fileOkay = false, fileSystem = fs)
        }

        Files.createDirectory(fs.getPath("/var"))
        Files.createFile(fs.getPath("/var/foo"))

        shouldThrow<BadParameterValue> {
            C().parse(splitArgv("-f/var/foo"))
        }.message shouldBe """Invalid value for "-f": Directory "/var/foo" is a file."""
    }

    @Test
    fun `folderOkay = false will reject folders`() {
        class C : NoRunCliktCommand() {
            val fileOnly by option("-f").path(folderOkay = false, fileSystem = fs)
        }

        Files.createDirectories(fs.getPath("/var/foo"))

        shouldThrow<BadParameterValue> {
            C().parse(splitArgv("-f/var/foo"))
        }.message shouldBe """Invalid value for "-f": File "/var/foo" is a directory."""
    }

    @Test
    fun `exists = true will reject paths that don't exist`() {
        class C : NoRunCliktCommand() {
            val homeDir by option("-h").path(exists = true, fileSystem = fs)
        }

        shouldThrow<BadParameterValue> {
            C().parse(splitArgv("-h /home/cli"))
        }.message shouldBe """Invalid value for "-h": Path "/home/cli" does not exist."""
    }
}
