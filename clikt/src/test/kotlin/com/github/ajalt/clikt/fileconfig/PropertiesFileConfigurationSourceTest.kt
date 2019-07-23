package com.github.ajalt.clikt.fileconfig

import com.github.ajalt.clikt.core.CliktFileFormatError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PropertiesFileConfigurationSourceTest {
    @get:Rule
    var testFolder = TemporaryFolder()

    @Test
    fun `single properties file`() {
        val file = testFolder.newFile()
        file.writeText("foo=bar")

        forall(
                row("--foo 1", file, "1"),
                row("", file, "bar"),
                row("", File("!nonexistent!"), null)
        ) { argv, f, expected ->
            class C : TestCommand() {
                init {
                    context {
                        valuesSource = PropertiesFileValuesSource(f)
                    }
                }

                val foo by option()
                val bar by option()

                override fun run_() {
                    foo shouldBe expected
                    bar shouldBe null
                }
            }

            C().parse(argv)
        }
    }

    @Test
    fun `two properties files`() {
        val file1 = testFolder.newFile()
        val file2 = testFolder.newFile()
        file2.writeText("foo=bar")

        class C : TestCommand() {
            init {
                context {
                    valueSources {
                        javaProperties(file1, file2)
                    }
                }
            }

            val foo by option()
            val bar by option()

            override fun run_() {
                foo shouldBe "bar"
                bar shouldBe null
            }
        }

        C().parse("")
    }

    @Test
    fun `invalid properties file`() {
        val file = testFolder.newFile()
        file.writeText("\\u1")

        class C(called: Boolean, requireValid: Boolean) : TestCommand(called) {
            init {
                context {
                    valueSources {
                        javaProperties(file, requireValid = requireValid)
                    }
                }
            }

            val foo by option()

            override fun run_() {
                foo shouldBe null
            }
        }

        C(called = true, requireValid = false).parse("")

        shouldThrow<CliktFileFormatError> {
            C(called = false, requireValid = true).parse("")
        }
    }

    @Test
    fun `option types from properties file`() {
        val file = testFolder.newFile()
        file.writeText("""
        int=11
        float=2.2
        double=3.3
        flag=true
        counted=4
        long-name=5
        """.trimIndent())

        class C : TestCommand() {
            init {
                context {
                    valueSources {
                        javaProperties(file.path)
                    }
                }
            }

            val int by option().int()
            val float by option().float()
            val double by option().double()
            val flag by option().flag()
            val counted by option().counted()
            val longName by option()

            override fun run_() {
                int shouldBe 11
                float shouldBe 2.2f
                double shouldBe 3.3
                flag shouldBe true
                counted shouldBe 4
                longName shouldBe "5"
            }
        }

        C().parse("")
    }
}
