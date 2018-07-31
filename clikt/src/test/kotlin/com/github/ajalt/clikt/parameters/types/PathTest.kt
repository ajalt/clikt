package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.testing.splitArgv
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotlintest.shouldBe
import org.junit.Test

class PathTest {
    @Test
    fun `paths are resolved using the provided filesystem, if any`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
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
}