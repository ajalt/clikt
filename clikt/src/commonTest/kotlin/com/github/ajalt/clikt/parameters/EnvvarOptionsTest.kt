package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.TestSource
import com.github.ajalt.clikt.testing.parse
import com.github.ajalt.clikt.testing.skipDueToKT43490
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class EnvvarOptionsTest {
    private val env: MutableMap<String, String?> = mutableMapOf()
    private fun <T : CliktCommand> T.withEnv(): T = context { envvarReader = env::get }

    @Test
    @JsName("explicit_envvar")
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

        C().withEnv().parse("")
    }

    @Test
    @JsName("auto_envvar")
    fun `auto envvar`() {
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

        C().context { autoEnvvarPrefix = "C" }.withEnv().parse("")
    }

    @Test
    @JsName("auto_envvar_subcommand")
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
            .withEnv().parse("cmd1 sub2 sub3")
    }

    @Test
    @JsName("split_envvar")
    fun `split envvar`() {
        env["FOO"] = "/home"

        class C : TestCommand() {
            val foo by option(envvar = "FOO")
            val bar by option(envvar = "BAR").split(";")
            override fun run_() {
                foo shouldBe "/home"
                bar shouldBe listOf("/bar", "/baz")
            }
        }

        env["BAR"] = "/bar;/baz"
        C().withEnv().parse("")
    }

    @Test
    @JsName("flag_envvars")
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

        C().withEnv().parse("")
        called1 shouldBe true
        called2 shouldBe true
    }

    @Test
    @JsName("readEnvvarBeforeValuesSource_when_both_exist")
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

        C().withEnv().parse("")
        source.assert(read = !envvarFirst)
    }

    @Test
    @JsName("cooccurring_option_group_envvar")
    fun `cooccurring option group envvar`() = forAll(
        row("", "xx", "yy"),
        row("--x=z", "z", "yy"),
        row("--y=z", "xx", "z"),
    ) { argv, ex, ey ->
        env["X"] = "xx"
        env["Y"] = "yy"

        class G : OptionGroup() {
            val x by option(envvar="X").required()
            val y by option(envvar="Y")
        }

        class C : TestCommand() {
            val g by G().cooccurring()

            override fun run_() {
                g?.x shouldBe ex
                g?.y shouldBe ey
            }
        }

        C().withEnv().parse(argv)
    }

    @Test
    @JsName("mutually_exclusive_option_group_envvar")
    fun `mutually exclusive option group envvar`() {
        class C: TestCommand() {
            val opt by mutuallyExclusiveOptions(
                option("--foo", envvar = "FOO"),
                option("--bar", envvar = "BAR"),
            ).single()
        }
        C().withEnv().parse("--bar=x").opt shouldBe "x"

        env["FOO"] = "y"
        C().withEnv().parse("").opt shouldBe "y"

        env["BAR"] = "z"
        if (skipDueToKT43490) return
        shouldThrow<MutuallyExclusiveGroupException> {
            C().withEnv().parse("")
        }
    }
}
