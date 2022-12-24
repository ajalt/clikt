package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.defaultLocalization
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.single
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
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
    private fun <T : CliktCommand> T.withEnv(vararg entries: Pair<String, String?>): T {
        return context { envvarReader = { entries.toMap()[it] } }
    }

    @Test
    @JsName("explicit_envvar")
    fun `explicit envvar`() {
        class C : TestCommand() {
            val foo by option(envvar = "FO")
            val bar by option()
            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe null
            }
        }

        C().withEnv("FO" to "foo").parse("")
    }

    @Test
    @JsName("auto_envvar")
    fun `auto envvar`() {
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

        C().context { autoEnvvarPrefix = "C" }.withEnv(
            "FO" to "foo",
            "C_BAR" to "11",
        ).parse("")
    }

    @Test
    @JsName("auto_envvar_subcommand")
    fun `auto envvar subcommand`() {
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
            .withEnv(
                "FOO" to "foo",
                "C_CMD1_BAR" to "bar",
                "BAZ" to "baz",
                "CMD2_QUX" to "qux",
                "CMD2_SUB3_QUZ" to "quz",
            ).parse("cmd1 sub2 sub3")
    }

    @Test
    @JsName("split_envvar")
    fun `split envvar`() {

        class C : TestCommand() {
            val foo by option(envvar = "FOO")
            val bar by option(envvar = "BAR").split(";")
            override fun run_() {
                foo shouldBe "/home"
                bar shouldBe listOf("/bar", "/baz")
            }
        }

        C().withEnv(
            "FOO" to "/home",
            "BAR" to "/bar;/baz",
        ).parse("")
    }

    @Test
    @JsName("flag_envvars")
    fun `flag envvars`() = forAll(
        row(null, null, false, 0),
        row("YES", "3", true, 3),
        row("false", "5", false, 5)
    ) { fv, bv, ef, eb ->
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

        C().withEnv(
            "FOO" to fv,
            "BAR" to bv,
        ).parse("")
        called1 shouldBe true
        called2 shouldBe true
    }

    @Test
    @JsName("vararg_option_envvars")
    fun `vararg option envvars`() = forAll(
        row(null, null, null, null),
        row("f", "b", listOf("f"), "b"),
    ) { fv, bv, ef, eb ->
        class C : TestCommand() {
            val foo: List<String>? by option(envvar = "FOO").varargValues()
            val bar: String? by option(envvar = "BAR").optionalValue("")
            override fun run_() {
                foo shouldBe ef
                bar shouldBe eb
            }
        }

        C().withEnv(
            "FOO" to fv,
            "BAR" to bv,
        ).parse("")
    }

    @Test
    @JsName("readEnvvarBeforeValuesSource_when_both_exist")
    fun `readEnvvarBeforeValuesSource when both exist`() = forAll(
        row(true, "bar"),
        row(false, "baz")
    ) { envvarFirst, expected ->
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

        C().withEnv(
            "FOO" to "bar",
        ).parse("")
        source.assert(read = !envvarFirst)
    }

    @Test
    @JsName("cooccurring_option_group_envvar")
    fun `cooccurring option group envvar`() = forAll(
        row("", "xx", "yy"),
        row("--x=z", "z", "yy"),
        row("--y=z", "xx", "z"),
    ) { argv, ex, ey ->
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

        C().withEnv("X" to "xx", "Y" to "yy").parse(argv)
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
        shouldThrow<MutuallyExclusiveGroupException> {
            C().withEnv().parse("")
        }
    }

    @Test
    @Suppress("unused")
    @JsName("switch_envvars")
    fun `switch envvar`(){
        class C : TestCommand() {
            val opt by option(envvar = "FOO").switch("--x" to 1, "--y" to 2)
        }

        shouldThrow<BadParameterValue> {
            C().withEnv("FOO" to "3").parse("")
        }.message shouldBe defaultLocalization.switchOptionEnvvar()
    }
}
