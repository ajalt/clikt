package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.TestSource
import com.github.ajalt.clikt.testing.parse
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties
import java.io.File
import kotlin.test.Test


class EnvvarOptionsTest {
    @Rule
    @JvmField
    val env = EnvironmentVariables()

    @Rule
    @JvmField
    val restoreSystemProperties = RestoreSystemProperties()

    @Test
    fun `explicit envvar`() {
        env["FO"] = "foo"

        class C : TestCommand() {
            val foo by option(envvar = "FO")
            val bar by option()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe null
            }
        }

        C().parse(emptyArray())
    }

    @Test
    fun `auto envvar`() {
        env["FO"] = "foo"
        env["FO"] = "foo"
        env["C_BAR"] = "11"

        class C : TestCommand() {
            val foo by option(envvar = "FO")
            val bar by option().int()
            val baz by option()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe 11
                baz shouldBe null
            }
        }

        C().context { autoEnvvarPrefix = "C" }.parse(emptyArray())
    }

    @Test
    fun `auto envvar subcommand`() {
        env["FOO"] = "foo"
        env["C_CMD1_BAR"] = "bar"
        env["BAZ"] = "baz"
        env["CMD2_QUX"] = "qux"
        env["CMD2_SUB3_QUZ"] = "quz"

        class C : TestCommand() {
            init {
                context { autoEnvvarPrefix = "C" }
            }
        }

        class Sub : TestCommand(name = "cmd1") {
            val foo by option(envvar = "FOO")
            val bar by option()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe "bar"
            }
        }

        class Sub2 : TestCommand() {
            init {
                context { autoEnvvarPrefix = "CMD2" }
            }

            val baz by option(envvar = "BAZ")
            val qux by option()
            override fun run_() {
                baz shouldBe "baz"
                qux shouldBe "qux"
            }
        }

        class Sub3 : TestCommand() {
            val quz by option()
            override fun run_() {
                quz shouldBe "quz"
            }
        }

        C().subcommands(Sub().subcommands(Sub2().subcommands(Sub3())))
                .parse("cmd1 sub2 sub3")
    }

    @Test
    fun `split envvar`() {
        env["FOO"] = "/home"

        class C : TestCommand() {
            val foo by option(envvar = "FOO").file()
            val bar by option(envvar = "BAR").file().split(";")
            override fun run_() {
                foo shouldBe File("/home")
                bar shouldBe listOf(File("/bar"), File("/baz"))
            }
        }

        env["BAR"] = "/bar;/baz"
        C().parse(emptyArray())
    }

    @Test
    fun `flag envvars`() = forAll(
            row(null, null, false, 0),
            row("YES", "3", true, 3),
            row("false", "5", false, 5)
    ) { fv, bv, ef, eb ->

        env["FOO"] = fv
        env["BAR"] = bv

        var called1 = false
        var called2 = false

        class C : TestCommand() {
            val foo by option(envvar = "FOO").flag("--no-foo").validate { called1 = true }
            val bar by option(envvar = "BAR").counted().validate { called2 = true }
            override fun run_() {
                foo shouldBe ef
                bar shouldBe eb
            }
        }

        C().parse(emptyArray())
        called1 shouldBe true
        called2 shouldBe true
    }

    @Test
    fun `readEnvvarBeforeValuesSource when both exist`() = forAll(
            row(true, "bar"),
            row(false, "baz")
    ) { envvarFirst, expected ->
        env["FOO"] = "bar"
        val source = TestSource("foo" to "baz")

        class C : TestCommand() {
            init {
                context {
                    valueSource = source
                    readEnvvarBeforeValueSource = envvarFirst
                }
            }

            val foo by option(envvar = "FOO")

            override fun run_() {
                foo shouldBe expected
            }
        }

        C().parse("")
        source.assert(read = !envvarFirst)
    }
}
