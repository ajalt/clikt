package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.testing.splitArgv
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotlintest.fail
import io.kotlintest.shouldBe
import java.nio.file.Files
import java.nio.file.FileSystem
import org.junit.Test

class PathTest {
    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Test
    fun `paths are resolved using the provided filesystem, if any`() {
        val command = object : CliktCommand() {
            val path by option("-p")
                    .path(fileSystem = fs)
                    .required()

            override fun run() {
                path.fileSystem shouldBe fs
            }
        }

        command.parse(splitArgv("-p/var/log/foo"))
    }

    @Test
    fun `options can be paths`() {
        val command = object : CliktCommand() {
            val path by option("-p")
                    .path()
                    .required()

            override fun run() {
                path.toString() shouldBe "foo"
            }
        }

        command.parse(splitArgv("-pfoo"))
    }

    @Test
    fun `arguments can be paths`() {
        val command = object : CliktCommand() {
            val paths by argument()
                    .path()
                    .multiple()

            override fun run() {
                paths.map { it.toString() } shouldBe listOf("foo", "bar", "baz")
            }
        }

        command.parse(splitArgv("foo bar baz"))
    }

    @Test
    fun `fileOkay = false will reject files`() {
        val command = object : CliktCommand() {
            val folderOnly by option("-f").path(fileOkay = false, fileSystem = fs)

            override fun run() {
            }
        }

        Files.createDirectory(fs.getPath("/var"))
        Files.createFile(fs.getPath("/var/foo"))

        expectBadParameter("Invalid value for \"-f\": Directory \"/var/foo\" is a file.") {
            command.parse(splitArgv("-f/var/foo"))
        }
    }

    @Test
    fun `folderOkay = false will reject folders`() {
        val command = object : CliktCommand() {
            val fileOnly by option("-f").path(folderOkay = false, fileSystem = fs)

            override fun run() {
            }
        }

        Files.createDirectories(fs.getPath("/var/foo"))

        expectBadParameter("Invalid value for \"-f\": File \"/var/foo\" is a directory.") {
            command.parse(splitArgv("-f/var/foo"))
        }
    }

    @Test fun `exists = true will reject paths that don't exist` () {
        val command = object : CliktCommand() {
            val homeDir by option("-h").path(exists = true, fileSystem = fs)

            override fun run() {
            }
        }

        expectBadParameter("Invalid value for \"-h\": Path \"/home/cli\" does not exist.") {
            command.parse(splitArgv("-h /home/cli"))
        }
    }

    private inline fun expectBadParameter(message: String, fn: () -> Unit) {
        try {
            fn()
            fail("parse should have failed with a BadParameterValue exception")
        } catch (e: BadParameterValue) {
            e.message shouldBe message
        }
    }
}