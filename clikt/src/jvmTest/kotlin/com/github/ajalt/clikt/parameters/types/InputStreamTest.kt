package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.TextFromStandardInputStream
import org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream
import java.nio.file.FileSystem
import java.nio.file.Files
import kotlin.test.Test


@Suppress("unused")
@OptIn(ExperimentalStdlibApi::class)
class InputStreamTest {
    @get:Rule
    val stdin: TextFromStandardInputStream = emptyStandardInputStream()
    val fs: FileSystem = Jimfs.newFileSystem(Configuration.unix())

    @Test
    fun `options can be inputStreams`() {
        val file = Files.createFile(fs.getPath("foo"))
        Files.write(file, "text".encodeToByteArray())

        class C : TestCommand() {
            val stream by option().inputStream(fs)

            override fun run_() {
                stream?.readBytes()?.decodeToString() shouldBe "text"
            }
        }

        C().parse("--stream=foo")
    }

    @Test
    fun `passing explicit -`() {
        stdin.provideLines("text")
        class C : TestCommand() {
            val stream by argument().inputStream(fs)

            override fun run_() {
                stream.readBytes().decodeToString().replace("\r", "") shouldBe "text\n"
            }
        }

        C().parse("-")
    }

    @Test
    fun `option and arg with defaultStdin`() {
        class C : TestCommand() {
            val option by option().inputStream(fs).defaultStdin()
            val stream by argument().inputStream(fs).defaultStdin()

            override fun run_() {
                stdin.provideLines("text1")
                option.readBytes().decodeToString().replace("\r", "") shouldBe "text1\n"

                stdin.provideLines("text2")
                stream.readBytes().decodeToString().replace("\r", "") shouldBe "text2\n"
            }
        }

        C().parse("")
    }

    @Test
    fun `option inputStream is defaultStdin`() {
        class C : TestCommand() {
            val option by option().inputStream(fs).defaultStdin()

            override fun run_() {
                option.isCliktParameterDefaultStdin.shouldBeTrue()
            }
        }

        C().parse("")
    }

    @Test
    fun `option inputStream is not defaultStdin`() {
        Files.createFile(fs.getPath("foo"))

        class C : TestCommand() {
            val option by option().inputStream(fs)

            override fun run_() {
                option?.isCliktParameterDefaultStdin?.shouldBeFalse()
            }
        }

        C().parse("--option=foo")
    }

    @Test
    fun `argument inputStream is defaultStdin`() {
        class C : TestCommand() {
            val stream by argument().inputStream(fs).defaultStdin()

            override fun run_() {
                stream.isCliktParameterDefaultStdin.shouldBeTrue()
            }
        }

        C().parse("")
    }

    @Test
    fun `argument inputStream is not defaultStdin`() {
        Files.createFile(fs.getPath("foo"))

        class C : TestCommand() {
            val stream by argument().inputStream(fs)

            override fun run_() {
                stream.isCliktParameterDefaultStdin.shouldBeFalse()
            }
        }

        C().parse("foo")
    }
}
