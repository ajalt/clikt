package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.float
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.Properties

class PropertiesValueSourceTest {
    @get:Rule
    var testFolder = TemporaryFolder()

    @Test
    fun `single properties file`() {
        val file = testFolder.newFile()
        file.writeText("foo=bar")

        forAll(
            row("--foo 1", file, "1"),
            row("", file, "bar"),
            row("", File("!nonexistent!"), null)
        ) { argv, f, expected ->
            class C : TestCommand() {
                init {
                    context {
                        valueSource = PropertiesValueSource.from(f)
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
                    valueSources(
                        PropertiesValueSource.from(file1.toPath()),
                        PropertiesValueSource.from(file2.toPath())
                    )
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
    fun `properties files with subcommands`() {
        val file = testFolder.newFile()
        file.writeText("sub.foo=bar\nOPTION=baz")

        class Root : TestCommand()
        class Sub : TestCommand() {
            init {
                context {
                    valueSource = PropertiesValueSource.from(file)
                }
            }

            val foo by option()
            val opt by option(valueSourceKey = "OPTION")

            override fun run_() {
                foo shouldBe "bar"
                opt shouldBe "baz"
            }
        }

        Root().subcommands(Sub()).parse("sub")
    }


    @Test
    fun `invalid properties file`() {
        val file = testFolder.newFile()
        file.writeText("\\u1")

        class C(called: Boolean, requireValid: Boolean) : TestCommand(called) {
            init {
                context {
                    valueSource = PropertiesValueSource.from(file, requireValid = requireValid)
                }
            }

            val foo by option()

            override fun run_() {
                foo shouldBe null
            }
        }

        C(called = true, requireValid = false).parse("")

        shouldThrow<InvalidFileFormat> {
            C(called = false, requireValid = true).parse("")
        }
    }

    @Test
    fun `option types from properties file`() {
        val file = testFolder.newFile()
        file.writeText(
            """
        int=11
        float=2.2
        double=3.3
        flag=true
        counted=4
        long-name=5
        """.trimIndent()
        )

        class C : TestCommand() {
            init {
                context {
                    valueSource = PropertiesValueSource.from(file.path)
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

    @Test
    fun errorMessage() {
        class C : TestCommand() {
            @Suppress("unused")
            val theInteger by option().int()

            @Suppress("unused")
            val theFlag by option(valueSourceKey = "that flag").flag()
        }

        shouldThrow<BadParameterValue> {
            val valueSource = PropertiesValueSource.from(Properties().apply { setProperty("the-integer", "foo") })
            C().apply { configureContext { this.valueSource = valueSource } }.parse("")
        }.formattedMessage shouldBe "invalid value for the-integer: foo is not a valid integer"

        shouldThrow<BadParameterValue> {
            val valueSource = PropertiesValueSource.from(Properties().apply { setProperty("that flag", "foo") })
            C().apply { configureContext { this.valueSource = valueSource } }.parse("")
        }.formattedMessage shouldBe "invalid value for that flag: foo is not a valid boolean"

        shouldThrow<BadParameterValue> {
            val properties = Properties().apply { setProperty("A_THE_INTEGER", "foo") }
            val valueSource = PropertiesValueSource.from(properties, getKey = ValueSource.envvarKey())
            C().apply {
                configureContext {
                    this.valueSource = valueSource
                    this.autoEnvvarPrefix = "A"
                }
            }.parse("")
        }.formattedMessage shouldBe "invalid value for A_THE_INTEGER: foo is not a valid integer"
    }
}
