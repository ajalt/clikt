package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemOutRule
import java.io.OutputStream
import java.nio.file.FileSystem
import java.nio.file.Files
import kotlin.test.Test


@Suppress("unused")
@OptIn(ExperimentalStdlibApi::class)
class OutputStreamTest {
    @get:Rule
    val stdout = SystemOutRule().enableLog()
    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    private fun OutputStream?.writeText(text: String) = this!!.bufferedWriter().use { it.write(text) }

    @Test
    fun `options can be outputStreams`() {
        class C : TestCommand() {
            val stream by option().outputStream(fileSystem = fs)

            override fun run_() {
                stream.writeText("bar")
                Files.readAllBytes(fs.getPath("foo")).decodeToString() shouldBe "bar"
            }
        }

        C().parse("--stream=foo")
    }

    @Test
    fun `passing explicit -`() {
        class C : TestCommand() {
            val stream by argument().outputStream(fileSystem = fs)

            override fun run_() {
                stream.writeText("foo")
                stdout.log shouldBe "foo"
            }
        }

        C().parse("-")
    }

    @Test
    fun `option and arg with defaultStdout`() {
        class C : TestCommand() {
            val option by option().outputStream(fileSystem = fs).defaultStdout()
            val stream by argument().outputStream(fileSystem = fs).defaultStdout()

            override fun run_() {
                option.writeText("foo")
                stdout.log shouldBe "foo"
                stdout.clearLog()

                stream.writeText("bar")
                stdout.log shouldBe "bar"
            }
        }

        C().parse("")
    }

    @Test
    fun `option outputStream is defaultStdout`() {
        class C : TestCommand() {
            val option by option().outputStream(fileSystem = fs).defaultStdout()

            override fun run_() {
                option.isCliktParameterDefaultStdout.shouldBeTrue()
            }
        }

        C().parse("")
    }

    @Test
    fun `option outputStream is not defaultStdout`() {
        class C : TestCommand() {
            val option by option().outputStream(fileSystem = fs)

            override fun run_() {
                option?.isCliktParameterDefaultStdout?.shouldBeFalse()
            }
        }

        C().parse("--option=foo")
    }

    @Test
    fun `argument outputStream is defaultStdout`() {
        class C : TestCommand() {
            val stream by argument().outputStream(fileSystem = fs).defaultStdout()

            override fun run_() {
                stream.isCliktParameterDefaultStdout.shouldBeTrue()
            }
        }

        C().parse("")
    }

    @Test
    fun `argument outputStream is not defaultStdout`() {
        class C : TestCommand() {
            val stream by argument().outputStream(fileSystem = fs)

            override fun run_() {
                stream.isCliktParameterDefaultStdout.shouldBeFalse()
            }
        }

        C().parse("foo")
    }

    @Test
    fun `createIfNotExist = false`() {
        class C : TestCommand(called = false) {
            val stream by argument().outputStream(createIfNotExist = false, fileSystem = fs)
        }

        shouldThrow<BadParameterValue> {
            C().parse("foo")
        }.message shouldContain "File \"foo\" does not exist"
    }

    @Test
    fun truncateExisting() = forAll(
            row(true, "baz"),
            row(false, "bar\nbaz")
    ) { truncateExisting, expected ->
        Files.write(fs.getPath("foo"), listOf("bar"))

        class C : TestCommand(called = true) {
            val stream by argument().outputStream(truncateExisting = truncateExisting, fileSystem = fs)

            override fun run_() {
                stream.writeText("baz")
                Files.readAllBytes(fs.getPath("foo")).decodeToString().replace("\r", "") shouldBe expected
            }
        }

        C().parse("foo")
    }
}
