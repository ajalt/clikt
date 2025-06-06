package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused", "BooleanLiteralArgument")
class OptionTest {
    @[Test JsName("zero_options")]
    fun `zero options`() {
        TestCommand(called = true).parse(arrayOf())
    }

    @[Test JsName("no_such_option")]
    fun `no such option`() = forAll(
        row("--qux", "no such option --qux"),
        row("--fo", "no such option --fo. Did you mean --foo?"),
        row("--fop", "no such option --fop. Did you mean --foo?"),
        row("--car", "no such option --car. Did you mean --bar?"),
        row("--ba", "no such option --ba. (Possible options: --bar, --baz)")
    ) { argv, message ->
        class C : TestCommand(called = false) {
            val foo by option()
            val bar by option()
            val baz by option()
            val fob by option(hidden = true)
        }

        val exception = shouldThrow<NoSuchOption> {
            C().parse(argv)
        }
        exception.formattedMessage shouldBe message
        exception.statusCode shouldBe 1
    }

    @[Test JsName("no_such_short_option_with_long")]
    fun `no such short option with long`() = forAll(
        row("-long", "no such option -l. Did you mean --long?"),
        row("-foo", "no such option -f. Did you mean --foo?"),
        row("-oof", "no such option -f. Did you mean --oof?"),
    ) { argv, message ->
        class C : TestCommand(called = false) {
            val short by option("-o").flag()
            val long by option()
            val foo by option()
            val oof by option()
        }

        shouldThrow<NoSuchOption> {
            C().parse(argv)
        }.formattedMessage shouldBe message
    }

    @[Test JsName("no_such_option_custom_localization")]
    fun `no such option custom localization`() {
        class L : Localization {
            override fun noSuchOption(name: String, possibilities: List<String>) =
                "custom message"
        }

        class C : TestCommand(called = false) {
            init {
                context { localization = L() }
            }
        }

        val c = C()
        val err = shouldThrow<NoSuchOption> {
            c.parse("-z")
        }
        c.getFormattedHelp(err) shouldContain "Error: custom message"
    }

    @[Test JsName("no_such_option_subcommand_hint")]
    fun `no such option subcommand hint`() {
        class C : TestCommand(called = false)
        class Sub : TestCommand(called = false) {
            val foo by option()
        }

        val c = C().subcommands(Sub())
        shouldThrow<NoSuchOption> {
            c.parse("--foo")
        }.formattedMessage shouldBe "no such option --foo. hint: sub has an option --foo"
    }

    @[Test JsName("one_option")]
    fun `one option`() = forAll(
        row("", null),
        row("--xx 3", "3"),
        row("--xx --xx", "--xx"),
        row("--xx=asd", "asd"),
        row("-x 4", "4"),
        row("-x -x", "-x"),
        row("-xfoo", "foo"),
        row("-x a=b", "a=b"),
        row("-xa=b", "a=b"),
        row("--xx a=b", "a=b"),
        row("--xx=a=b", "a=b")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @[Test JsName("two_options_one_name_each")]
    fun `two options one name each`() {
        class C : TestCommand() {
            val x by option("-x")
            val y by option("--yy")
            override fun run_() {
                x shouldBe "3"
                y shouldBe "4"
            }
        }
        C().parse("-x 3 --yy 4")
    }

    @[Test JsName("two_options")]
    fun `two options`() = forAll(
        row("--xx 3 --yy 4", "3", "4"),
        row("-x 3 --yy 4", "3", "4"),
        row("-x3 --yy 4", "3", "4"),
        row("--xx 3 -y4", "3", "4"),
        row("--xx=3 --yy=4", "3", "4"),
        row("-x3 --yy=4", "3", "4"),
        row("-x 3 -y 4", "3", "4"),
        row("-x3 -y 4", "3", "4"),
        row("-x 3 -y4", "3", "4"),
        row("-x3 -y4", "3", "4"),
        row("--yy 4", null, "4"),
        row("--yy=4", null, "4"),
        row("-y 4", null, "4"),
        row("-y4", null, "4"),
        row("--xx 3", "3", null),
        row("--xx=3", "3", null),
        row("-x 3", "3", null),
        row("-x3", "3", null)
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            val y by option("-y", "--yy")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }


    @[Test JsName("two_options_nvalues_2")]
    fun `two options nvalues=2`() = forAll(
        row("", null, null),
        row("--yy 5 7", null, "5" to "7"),
        row("--xx 1 3 --yy 5 7", "1" to "3", "5" to "7"),
        row("--xx 1 3 -y 5 7", "1" to "3", "5" to "7"),
        row("-x 1 3 --yy 5 7", "1" to "3", "5" to "7"),
        row("-x1 3 --yy 5 7", "1" to "3", "5" to "7"),
        row("--xx 1 3 -y5 7", "1" to "3", "5" to "7"),
        row("--xx=1 3 --yy=5 7", "1" to "3", "5" to "7"),
        row("-x1 3 --yy=5 7", "1" to "3", "5" to "7"),
        row("-x 1 3 -y 5 7", "1" to "3", "5" to "7"),
        row("-x1 3 -y 5 7", "1" to "3", "5" to "7"),
        row("-x 1 3 -y5 7", "1" to "3", "5" to "7"),
        row("-x1 3 -y5 7", "1" to "3", "5" to "7")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x", "--xx").pair()
            val y by option("-y", "--yy").pair()
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @[Test JsName("two_options_nvalues_3")]
    fun `two options nvalues=3`() {
        val xvalue = Triple("1", "2", "3")
        val yvalue = Triple("5", "6", "7")
        forAll(
            row("", null, null),
            row("--yy 5 6 7", null, yvalue),
            row("--xx 1 2 3 --yy 5 6 7", xvalue, yvalue),
            row("--xx 1 2 3 -y 5 6 7", xvalue, yvalue),
            row("-x 1 2 3 --yy 5 6 7", xvalue, yvalue),
            row("-x1 2 3 --yy 5 6 7", xvalue, yvalue),
            row("--xx 1 2 3 -y5 6 7", xvalue, yvalue),
            row("--xx=1 2 3 --yy=5 6 7", xvalue, yvalue),
            row("-x1 2 3 --yy=5 6 7", xvalue, yvalue),
            row("-x 1 2 3 -y 5 6 7", xvalue, yvalue),
            row("-x1 2 3 -y 5 6 7", xvalue, yvalue),
            row("-x 1 2 3 -y5 6 7", xvalue, yvalue),
            row("-x1 2 3 -y5 6 7", xvalue, yvalue)
        ) { argv, ex, ey ->
            class C : TestCommand() {
                val x by option("-x", "--xx").triple()
                val y by option("-y", "--yy").triple()
                override fun run_() {
                    x shouldBe ex
                    y shouldBe ey
                }
            }

            C().parse(argv)
        }
    }

    @[Test JsName("two_options_nvalues_2_usage_errors")]
    fun `two options nvalues=2 usage errors`() {
        class C : TestCommand(called = false) {
            val x by option("-x", "--xx").pair()
            val y by option("-y", "--yy").pair()
        }
        shouldThrow<IncorrectOptionValueCount> { C().parse("-x") }.formattedMessage shouldBe
                "option -x requires 2 values"
        shouldThrow<NoSuchArgument> { C().parse("--yy foo bar baz") }.formattedMessage shouldBe
                "got unexpected extra argument (baz)"
    }

    @[Test JsName("two_options_with_split")]
    fun `two options with split`() = forAll(
        row("", null, null),
        row("-x 5 -y a", listOf(5), listOf("a")),
        row("-x 5,6 -y a:b", listOf(5, 6), listOf("a", "b"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x").int().split(",")
            val y by option("-y").split(Regex(":"))
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @[Test JsName("two_options_with_split_and_limit")]
    fun `two options with split and limit`() = forAll(
        row("", null, null),
        row("-x 5 -y a", listOf("5"), listOf("a")),
        row("-x 5x6X7x8 -y a:b:c", listOf("5", "6", "7x8"), listOf("a", "b:c"))
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("-x").split("x", ignoreCase = true, limit = 3)
            val y by option("-y").split(Regex(":"), limit = 2)
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @[Test JsName("flag_options")]
    fun `flag options`() = forAll(
        row("", false, false, null),
        row("-xx", true, false, null),
        row("-xX", false, false, null),
        row("-Xx", true, false, null),
        row("-x --no-xx", false, false, null),
        row("--xx", true, false, null),
        row("--no-xx", false, false, null),
        row("--no-xx --xx", true, false, null),
        row("-y", false, true, null),
        row("--yy", false, true, null),
        row("-xy", true, true, null),
        row("-yx", true, true, null),
        row("-x -y", true, true, null),
        row("--xx --yy", true, true, null),
        row("-x -y -z foo", true, true, "foo"),
        row("--xx --yy --zz foo", true, true, "foo"),
        row("-xy -z foo", true, true, "foo"),
        row("-xyzxyz", true, true, "xyz"),
        row("-xXyzXyz", false, true, "Xyz"),
        row("-xzfoo", true, false, "foo")
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x: Boolean by option("-x", "--xx").flag("-X", "--no-xx")
            val y: Boolean by option("-y", "--yy").flag()
            val z: String? by option("-z", "--zz")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @[Test JsName("flag_convert")]
    fun `flag convert`() = forAll(
        row("", E.B, "false"),
        row("-xx", E.A, "false"),
        row("-xX", E.B, "false"),
        row("-Xx", E.A, "false"),
        row("-x --no-xx", E.B, "false"),
        row("--xx", E.A, "false"),
        row("--no-xx", E.B, "false"),
        row("--no-xx --xx", E.A, "false"),
        row("-y", E.B, "true"),
        row("--yy", E.B, "true"),
        row("-xy", E.A, "true"),
        row("-yx", E.A, "true"),
        row("-x -y", E.A, "true"),
        row("--xx --yy", E.A, "true")
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x: E by option("-x", "--xx").flag("-X", "--no-xx")
                .convert { if (it) E.A else E.B }
            val y: String by option("-y", "--yy").flag()
                .convert { it.toString() }

            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @[Test JsName("flag_convert_validate")]
    fun `flag convert validate`() {
        class C : TestCommand() {
            val x by option().flag("--no-x")
                .convert {
                    if (it) E.A else E.B
                }
                .validate {
                    require(it == E.A)
                }
        }

        C().parse("--x").x shouldBe E.A
        shouldThrow<UsageError> { C().parse("--no-x") }
    }

    @[Test JsName("counted_options")]
    fun `counted options`() = forAll(
        row("", 0, false, null),
        row("-x -x", 2, false, null),
        row("-xx", 2, false, null),
        row("-xx -xx", 4, false, null),
        row("--xx -y --xx", 2, true, null),
        row("--xx", 1, false, null),
        row("-y", 0, true, null),
        row("--yy", 0, true, null),
        row("-xy", 1, true, null),
        row("-yx", 1, true, null),
        row("-x -y", 1, true, null),
        row("--xx --yy", 1, true, null),
        row("-x -y -z foo", 1, true, "foo"),
        row("--xx --yy --zz foo", 1, true, "foo"),
        row("-xy -z foo", 1, true, "foo"),
        row("-xyx", 2, true, null),
        row("-xyxzxyz", 2, true, "xyz"),
        row("-xyzxyz", 1, true, "xyz"),
        row("-xzfoo", 1, false, "foo"),
        row("-xxxxxx", 4, false, null),
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option("-x", "--xx").counted(limit = 4)
            val y by option("-y", "--yy").flag()
            val z by option("-z", "--zz")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @[Test JsName("counted_option_clamp_false")]
    fun `counted option clamp=false`() {
        class C(called: Boolean) : TestCommand(called) {
            val x by option("-x").counted(limit = 2, clamp = false)
        }

        C(true).parse("").x shouldBe 0
        C(true).parse("-xx").x shouldBe 2

        shouldThrow<UsageError> { C(false).parse("-xxxx") }
            .formattedMessage shouldBe "invalid value for -x: option was given 4 times, but only 2 times are allowed"
    }

    @[Test JsName("default_option")]
    fun `default option`() = forAll(
        row("", "def"),
        row("-x4", "4")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx").default("def")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @[Test JsName("defaultLazy_option")]
    fun `defaultLazy option`() = forAll(
        row("", "default", true),
        row("-xbar", "bar", false)
    ) { argv, expected, ec ->
        var called = false

        class C : TestCommand() {
            val x by option("-x", "--x").defaultLazy { called = true; "default" }
            override fun run_() {
                x shouldBe expected
                called shouldBe ec
            }
        }

        called shouldBe false
        C().parse(argv)
    }

    @[Test JsName("defaultLazy_option_referencing_other_option")]
    fun `defaultLazy option referencing other option`() {
        class C : TestCommand() {
            val y by option().defaultLazy { x }
            val x by option().default("def")
            override fun run_() {
                y shouldBe "def"
            }
        }

        C().parse("")
    }


    @[Test JsName("defaultLazy_option_referencing_required_option")]
    fun `defaultLazy option referencing required option`() {
        class C : TestCommand(called = false) {
            val y by option().defaultLazy { x }
            val x by option().required()
        }

        shouldThrow<MissingOption> {
            C().parse("")
        }.formattedMessage shouldBe "missing option --x"
    }

    @[Test JsName("defaultLazy_option_referencing_argument")]
    fun `defaultLazy option referencing argument`() {
        class C : TestCommand() {
            val y by option().defaultLazy { x }
            val x by argument().default("def")
            override fun run_() {
                y shouldBe "def"
            }
        }

        C().parse("")
    }

    @[Test JsName("defaultLazy_option_mutual_recursion")]
    fun `defaultLazy option mutual recursion`() {
        class C : TestCommand() {
            val y: String by option().defaultLazy { x }
            val x: String by option().defaultLazy { y }
            override fun run_() {
                y shouldBe "def"
            }
        }
        shouldThrow<IllegalStateException> { C().parse("") }
    }

    @[Test JsName("required_option")]
    fun `required option`() {
        class C : TestCommand() {
            val x by option().required()
            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().parse("--x=foo")

        shouldThrow<MissingOption> {
            C().parse("")
        }.formattedMessage shouldBe "missing option --x"
    }

    @[Test JsName("multiple_option_default")]
    fun `multiple option default`() {
        class C : TestCommand() {
            val x: List<String> by option().multiple()
            override fun run_() {
                x shouldBe listOf()
            }
        }

        C().parse("")
    }

    @[Test JsName("multiple_option_custom_default")]
    fun `multiple option custom default`() {
        class C : TestCommand() {
            val x by option().multiple(listOf("foo"))
            override fun run_() {
                x shouldBe listOf("foo")
            }
        }

        C().parse("")
    }

    @[Test JsName("multiple_with_unique_option_default")]
    fun `multiple with unique option default`() {
        val command = object : TestCommand() {
            val x by option().multiple().unique()
            override fun run_() {
                x shouldBe emptySet()
            }
        }

        command.parse("")
    }

    @[Test JsName("multiple_with_unique_option_custom_default")]
    fun `multiple with unique option custom default`() {
        val command = object : TestCommand() {
            val x by option().multiple(listOf("foo", "bar", "bar")).unique()
            override fun run_() {
                x shouldBe setOf("foo", "bar")
            }
        }

        command.parse("")
    }

    @[Test JsName("multiple_with_unique_option_parsed")]
    fun `multiple with unique option parsed`() = forAll(
        row("--arg foo", setOf("foo")),
        row("--arg foo --arg bar --arg baz", setOf("foo", "bar", "baz")),
        row("--arg foo --arg foo --arg foo", setOf("foo"))
    ) { argv, expected ->
        val command = object : TestCommand() {
            val arg: Set<String> by option().multiple().unique()
            override fun run_() {
                arg shouldBe expected
            }
        }
        command.parse(argv)
    }

    @[Test JsName("split_with_unique_option_default")]
    fun `split with unique option default`() {
        val command = object : TestCommand() {
            val x: Set<String> by option().split(",").default(emptyList()).unique()
            override fun run_() {
                x shouldBe setOf("1", "2")
            }
        }

        command.parse("--x=1,2,1")
    }

    @[Test JsName("multiple_required_option")]
    fun `multiple required option`() {
        class C(called: Boolean) : TestCommand(called) {
            val x: List<String> by option().multiple(required = true)
        }

        C(true).apply { parse("--x 1"); x shouldBe listOf("1") }
        C(true).apply { parse("--x 2 --x 3"); x shouldBe listOf("2", "3") }

        shouldThrow<MissingOption> { C(false).parse("") }
            .formattedMessage shouldBe "missing option --x"
    }

    @[Test JsName("option_metavars")]
    fun `option metavars`() = forAll(
        row("--x", "text"),
        row("--y", "FOO"),
        row("--z", "foo"),
        row("--w", "bar"),
        row("--t", "value"),
        row("--u", null),
        row("--help", null),
    ) { opt, metavar ->
        class C : TestCommand() {
            val x by option()
            val y by option(metavar = "FOO").default("")
            val z by option(metavar = "foo").convert("BAR") { it }
            val w by option().convert("bar") { it }.convert("bar") { it }
            val t by option().convert { it }
            val u by option().flag()
            override fun run_() {
                registeredOptions().first { opt in it.names }
                    .metavar(currentContext) shouldBe metavar
            }
        }

        C().parse("")
    }

    @[Test JsName("option_validator_basic")]
    fun `option validator basic`() {
        var called = false

        class C : TestCommand() {
            val x by option().validate {
                called = true
                require(it == "foo") { "invalid value $it" }
            }
        }

        with(C()) {
            parse("--x=foo")
            x shouldBe "foo"
        }
        called shouldBe true

        called = false
        C().parse("")
        called shouldBe false
    }

    @[Test JsName("option_check")]
    fun `option check`() = forAll(
        row("--x=bar --y=foo --w=foo", "invalid value for --x: bar"),
        row("--y=bar --w=foo", "invalid value for --y: bar"),
        row("--y=foo --z=bar --w=foo", "invalid value for --z: fail"),
        row("--y=foo --w=bar", "invalid value for --w: fail bar")
    ) { argv, message ->
        class C : TestCommand() {
            val x by option().check { it == "foo" }
            val y by option().required().check { it == "foo" }

            val z by option().check("fail") { it == "foo" }
            val w by option().required().check(lazyMessage = { "fail $it" }) { it == "foo" }
        }

        shouldThrow<BadParameterValue> { C().parse(argv) }.formattedMessage shouldBe message
    }

    @[Test JsName("option_validator_required")]
    fun `option validator required`() {
        var called = false

        class C : TestCommand() {
            val x by option().required().validate {
                called = true
                require(it == "foo") { "invalid value $it" }
            }

            override fun run_() {
                x shouldBe "foo"
            }
        }

        C().parse("--x=foo")
        called shouldBe true

        called = false
        shouldThrow<MissingOption> { C().parse("") }
    }

    @[Test JsName("option_validator_flag")]
    fun `option validator flag`() {
        var called = false

        class C : TestCommand() {
            val x by option().flag().validate {
                called = true
                require(it)
            }

            override fun run_() {
                x shouldBe true
            }
        }

        C().parse("--x")
        called shouldBe true
    }


    @[Test JsName("convert_catches_exceptions")]
    fun `convert catches exceptions`() {
        class C : TestCommand(called = false) {
            init {
                context { allowInterspersedArgs = false }
            }

            val x by option().convert {
                when (it) {
                    "uerr" -> fail("failed")
                    "err" -> throw NumberFormatException("failed")
                }
                it
            }
        }

        shouldThrow<BadParameterValue> { C().parse("--x=uerr") }.paramName shouldBe "--x"
        shouldThrow<BadParameterValue> { C().parse("--x=err") }.paramName shouldBe "--x"
    }

    @[Test JsName("one_option_with_slash_prefix")]
    fun `one option with slash prefix`() = forAll(
        row("", null),
        row("/xx 3", "3"),
        row("/xx=asd", "asd"),
        row("/x 4", "4"),
        row("/x /xx /xx foo", "foo"),
        row("/xfoo", "foo")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("/x", "/xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @[Test JsName("one_option_with_java_prefix")]
    fun `one option with java prefix`() = forAll(
        row("", null),
        row("-xx 3", "3"),
        row("-xx=asd", "asd"),
        row("-x 4", "4"),
        row("-x -xx -xx foo", "foo"),
        row("-xfoo", "foo")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "-xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @[Test JsName("two_options_with_chmod_prefixes")]
    fun `two options with chmod prefixes`() = forAll(
        row("", false, false),
        row("-x", false, false),
        row("-x +x", true, false),
        row("+x -x", false, false),
        row("+y", false, true),
        row("-y", false, false),
        row("-y +y", false, true),
        row("+y -y", false, false),
        row("-x -y", false, false),
        row("-x -y +xy", true, true)
    ) { argv, ex, ey ->
        class C : TestCommand() {
            val x by option("+x").flag("-x")
            val y by option("+y").flag("-y")
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @[Test JsName("option_with_question_mark_name")]
    fun `option with question mark name`() = forAll(
        row("", null),
        row("-? 3", "3"),
        row("-?foo", "foo"),
        row("/? asd", "asd"),
        row("-? -? -? foo", "foo"),
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-?", "/?")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().parse(argv)
    }

    @[Test JsName("normalized_tokens")]
    fun `normalized tokens`() = forAll(
        row("", null),
        row("--XX=FOO", "FOO"),
        row("--xx=FOO", "FOO"),
        row("-XX", "X")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().context { transformToken = { it.lowercase() } }.parse(argv)
    }

    @[Test JsName("token_transform_alias")]
    fun `token transform alias`() = forAll(
        row("", null),
        row("--yy 3", "3")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-x", "--xx")
            override fun run_() {
                x shouldBe expected
            }
        }

        C().context { transformToken = { "--xx" } }.parse(argv)
    }

    @[Test JsName("deprecated_warning_options")]
    fun `deprecated warning options`() {
        class C : TestCommand() {
            val g by option()
            val f by option().flag().deprecated()
            val x by option().deprecated()
            val y by option().deprecated("warn")
            val z by option().deprecated()
            override fun run_() {
                messages shouldBe listOf(
                    "WARNING: option --f is deprecated",
                    "WARNING: option --x is deprecated",
                    "warn"
                )
            }
        }
        C().context { printExtraMessages = false }.parse("--g=0 --f --x=1 --y=2")
    }

    @[Test JsName("deprecated_error_option")]
    fun `deprecated error option`() {
        class C : TestCommand(called = false) {
            val x by option().flag().deprecated(error = true)
            val y by option().deprecated("err", error = true)
        }
        shouldThrow<CliktError> { C().parse("--x") }
            .formattedMessage shouldBe "ERROR: option --x is deprecated"

        shouldThrow<CliktError> { C().parse("--y=1") }
            .formattedMessage shouldBe "err"
    }

    @[Test JsName("options_with_chained_convert")]
    fun `options with chained convert`() = forAll(
        row("", null),
        row("--x=1", listOf(1))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option(names = arrayOf()).int().convert { listOf(it) }
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("associate_options")]
    fun `associate options`() = forAll(
        row("", emptyMap()),
        row("-Xfoo=bar", mapOf("foo" to "bar")),
        row("-Xfoo=bar -X baz=qux", mapOf("foo" to "bar", "baz" to "qux")),
        row("-Xfoo=bar -Xfoo=baz", mapOf("foo" to "baz")),
        row("-Xfoo -Xbaz=qux", mapOf("foo" to "", "baz" to "qux"))
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-X").associate()
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("associate_conversion_options")]
    fun `associate conversion options`() = forAll(
        row("", emptyMap(), emptyMap(), emptyMap()),
        row(
            "-Xa=1 -Yb=2 -Zc=3", mapOf("A" to 1), mapOf("B" to "2"), mapOf("c" to 3),
        )
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option("-X").associate { (k, v) -> k.uppercase() to v.toInt() }
            val y by option("-Y").associateBy { it.uppercase() }
            val z by option("-Z").associateWith { it.toInt() }
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }
        C().parse(argv)
    }


    @[Test JsName("customized_splitPair")]
    fun `customized splitPair`() = forAll(
        row("", null),
        row("-Xfoo:1", "foo|1"),
        row("-Xfoo:1 -Xbar:2", "bar|2"),
        row("-Xfoo:1 -Xfoo", "foo|"),
        row("-Xfoo:=", "foo|="),
        row("-Xfoo:1=1", "foo|1=1")
    ) { argv, expected ->
        class C : TestCommand() {
            val x by option("-X").splitPair(":").convert { "${it.first}|${it.second}" }
            override fun run_() {
                x shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("disable_allowGroupedShortOptions")]
    fun `disable allowGroupedShortOptions`() {
        class C(called: Boolean) : TestCommand(called) {
            init {
                context { allowGroupedShortOptions = false }
            }

            val x by option("-x").flag()
            val y by option("-y")
        }

        with(C(true).parse("-x -y 1")) {
            x shouldBe true
            y shouldBe "1"
        }

        shouldThrow<NoSuchOption> {
            C(false).parse("-xy")
        }.formattedMessage shouldBe "no such option -xy. Did you mean -x?"
    }
}


private enum class E { A, B }
