package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.groups.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.test
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test


class PlaintextHelpFormatterTest {
    private val c = TestCommand(name = "prog").context {
        helpFormatter = { PlaintextHelpFormatter(it) }
    }

    private fun doTest(
        expected: String,
        command: CliktCommand = c,
        helpNames: Set<String> = emptySet(),
    ) {
        command.context {
            helpOptionNames = helpNames
        }.getFormattedHelp() shouldBe expected.trimMargin()
    }

    @Test
    fun formatUsage() = forAll(
        row(listOf(), "Usage: prog"),
        row(listOf(c.option("-x")), "Usage: prog [<options>]"),
        row(listOf(c.argument("foo").optional()), "Usage: prog [<foo>]"),
        row(listOf(c.argument("foo")), "Usage: prog <foo>"),
        row(listOf(c.argument("foo").multiple()), "Usage: prog [<foo>]..."),
        row(listOf(c.argument("foo").multiple(required = true)), "Usage: prog <foo>..."),
        row(
            listOf<Any>(
                c.argument("FOO").multiple(required = true),
                c.option("-x"),
                c.argument("BAR").optional()
            ),
            "Usage: prog [<options>] <foo>... [<bar>]"
        ),
        row(
            listOf(c.option("-x"), c.argument("FOO").optional(), TestCommand(name = "bar")),
            "Usage: prog [<options>] [<foo>] <command> [<args>]..."
        )
    ) { params, expected ->
        val c = TestCommand(name = "prog").context {
            helpOptionNames = emptySet()
        }
        for (p in params) {
            when (p) {
                is Argument -> c.registerArgument(p)
                is Option -> c.registerOption(p)
                is CliktCommand -> c.subcommands(p)
                else -> error("Unknown param type: $p")
            }
        }
        c.getFormattedHelp()?.lines()?.first() shouldBe expected
    }

    @Test
    @JsName("formatUsage_narrow_width")
    fun `formatUsage narrow width`() {
        c.registerOption(c.option("-x"))
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -x=<text>
            """,
        )
    }


    @Test
    @JsName("help_output_one_opt")
    fun `one opt`() {
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by"))
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  some thing to live by
            """
        )
    }

    @Test
    @JsName("help_output_number_opt")
    fun `number opt`() {
        c.registerOption(
            c.option("--aa", "-a", help = "some thing to live by")
                .int(acceptsValueWithoutName = true)
        )
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -<int>, -a, --aa=<int>  some thing to live by
            """
        )
    }

    @Test
    @JsName("help_output_one_opt_secondary_name")
    fun `one opt secondary name`() {
        c.registerOption(
            c.option("--aa", "-a", help = "some thing to live by").flag("--no-aa", "-A")
        )
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa / -A, --no-aa  some thing to live by
            """
        )
    }

    @Test
    @JsName("help_output_one_opt_prolog")
    fun `one opt prolog`() {
        val c = TestCommand(
            name = "prog",
            help = "Lorem Ipsum.",
            epilog = "Dolor Sit Amet."
        )
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by").int())
        doTest(
            """
            |Usage: prog [<options>]
            |
            |  Lorem Ipsum.
            |
            |Options:
            |  -a, --aa=<int>  some thing to live by
            |
            |Dolor Sit Amet.
            """,
            command = c
        )
    }

    @Test
    @Suppress("unused")
    @JsName("help_output_option_groups")
    fun `option groups`() {
        class G : OptionGroup("Grouped") {
            override val groupHelp: String =
                "This is the help text for the option group named Grouped. " +
                        "This text should wrap onto exactly three lines."
            val a by option("--aa", "-a", help = "some thing to live by aa")
            val c by option("--cc", "-c", help = "some thing to live by cc")
        }

        class G2 : OptionGroup("Singleton") {
            val b by option("--bb", "-b", help = "some thing to live by bb")
        }

        class C : TestCommand(name = "prog") {
            val g by G()
            val g2 by G2()
            val o by option("--dd", "-d", help = "some thing to live by dd")
        }

        doTest(
            """
            |Usage: prog [<options>]
            |
            |Grouped:
            |
            |  This is the help text for the option group named Grouped. This text should wrap onto exactly three lines.
            |
            |  -a, --aa=<text>  some thing to live by aa
            |  -c, --cc=<text>  some thing to live by cc
            |
            |Singleton:
            |  -b, --bb=<text>  some thing to live by bb
            |
            |Options:
            |  -d, --dd=<text>  some thing to live by dd
            """,
            command = C()
        )
    }

    @Test
    @Suppress("unused")
    @JsName("help_output_arguments")
    fun arguments() {
        class C : TestCommand(name = "prog") {
            val foo by argument(help = "some thing to live by")
            val bar by argument(help = "another argument").optional()
        }
        doTest(
            """
            |Usage: prog <foo> [<bar>]
            |
            |Arguments:
            |  <foo>  some thing to live by
            |  <bar>  another argument
            """,
            command = C()
        )
    }

    @Test
    @JsName("subcommands")
    fun subcommands() {
        c.subcommands(
            TestCommand(name = "foo", help = "some thing to live by"),
            TestCommand(name = "bar", help = "another argument"),
            TestCommand(name = "baz"),
            TestCommand(name = "qux", hiddenFromHelp = true),
        )
        doTest(
            """
            |Usage: prog <command> [<args>]...
            |
            |Commands:
            |  foo  some thing to live by
            |  bar  another argument
            |  baz
            """
        )
    }

    @Test
    @JsName("help_output_optional_values")
    fun `optional values`() {
        c.registerOption(c.option("--foo", "-f", help = "option one").optionalValue("d1"))
        c.registerOption(
            c.option("--bar", help = "option two").choice("c1", "c2").optionalValue("d2")
        )
        c.registerOption(c.option("--baz", help = "option three").int().varargValues(min = 0))
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -f, --foo[=<text>]  option one
            |  --bar[=(c1|c2)]     option two
            |  --baz[=<int>...]    option three
            """
        )
    }

    @Test
    @Suppress("unused")
    @JsName("choice_group")
    fun `choice group`() {
        class G1 : OptionGroup("G1") {
            val opt1 by option()
        }

        class G2 : OptionGroup("G2") {
            val opt2 by option()
        }

        class C : TestCommand() {
            val opt by option(help = "select group").groupChoice("g1" to G1(), "g2" to G2())
        }
        doTest(
            """
            |Usage: c [<options>]
            |
            |G1:
            |  --opt1=<text>
            |
            |G2:
            |  --opt2=<text>
            |
            |Options:
            |  --opt=(g1|g2)  select group
            """,
            command = C()
        )
    }

    @Test
    @Suppress("unused")
    @JsName("switch_group")
    fun `switch group`() {
        class G1 : OptionGroup("G1") {
            val opt1 by option()
        }

        class G2 : OptionGroup("G2") {
            val opt2 by option()
        }

        class C : TestCommand() {
            val opt by option(help = "select group").groupSwitch("--g1" to G1(), "--g2" to G2())
        }

        doTest(
            """
            |Usage: c [<options>]
            |
            |G1:
            |  --opt1=<text>
            |
            |G2:
            |  --opt2=<text>
            |
            |Options:
            |  --g1, --g2  select group
            """,
            command = C()
        )
    }

    @Test
    @Suppress("unused")
    @JsName("mutually_exclusive_options")
    fun `mutually exclusive options`() {
        class C : TestCommand(name = "prog") {
            val ex by mutuallyExclusiveOptions(
                option("--ex-foo", help = "exclusive foo"),
                option("--ex-bar", help = "exclusive bar")
            ).help(
                name = "Exclusive", help = "These options are exclusive"
            )
            val ex2 by mutuallyExclusiveOptions(
                option("--ex-baz", help = "exclusive baz"),
                option("--ex-qux", help = "exclusive qux"),
                option("--ex-quz", help = "exclusive quz"),
                name = "Exclusive without help"
            )
        }
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Exclusive:
            |
            |  These options are exclusive
            |
            |  --ex-foo=<text>  exclusive foo
            |  --ex-bar=<text>  exclusive bar
            |
            |Exclusive without help:
            |  --ex-baz=<text>  exclusive baz
            |  --ex-qux=<text>  exclusive qux
            |  --ex-quz=<text>  exclusive quz
            """,
            command = C()
        )
    }

    @Test
    @JsName("eager_options")
    fun `eager options`() {
        c.versionOption("1.0")
        c.eagerOption(
            "--eager",
            "-e",
            help = "this is an eager option with a group",
            groupName = "My Group"
        ) {}
        c.eagerOption("--eager2", "-E", help = "this is an eager option") {}
        doTest(
            """
            |Usage: prog [<options>]
            |
            |My Group:
            |  -e, --eager  this is an eager option with a group
            |
            |Options:
            |  --version     Show the version and exit
            |  -E, --eager2  this is an eager option
            |  -h, --help    Show this message and exit
            """,
            helpNames = setOf("-h", "--help")
        )
    }

    @Test
    @JsName("required_option_marker")
    fun `required option marker`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(c.option("--bb", "-b", help = "bb option help").required())
        c.context { helpFormatter = { PlaintextHelpFormatter(it, requiredOptionMarker = "*") } }
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  aa option help
            |* -b, --bb=<text>  bb option help
            """
        )
    }

    @Test
    @JsName("required_option_tag")
    fun `required option tag`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(c.option("--bb", "-b", help = "bb option help").required())
        c.context { helpFormatter = { PlaintextHelpFormatter(it, showRequiredTag = true) } }
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  aa option help
            |  -b, --bb=<text>  bb option help (required)
            """
        )
    }

    @Test
    @JsName("default_option_tag")
    fun `default option tag`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(c.option("--bb", "-b", help = "bb option help").default("123"))
        c.context { helpFormatter = { PlaintextHelpFormatter(it, showDefaultValues = true) } }
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  aa option help
            |  -b, --bb=<text>  bb option help (default: 123)
            """
        )
    }

    @Test
    @JsName("custom_tag")
    fun `custom tag`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(
            c.option(
                "--bb",
                "-b",
                help = "bb option help",
                helpTags = mapOf("deprecated" to "")
            )
        )
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  aa option help
            |  -b, --bb=<text>  bb option help (deprecated)
            """
        )
    }

    @Test
    @JsName("option_tag_and_markers")
    fun `option tag and markers`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(
            c.option(
                "--bb", "-b", help = "bb option help",
                helpTags = mapOf("t1" to "", "t2" to "v2")
            ).required()
        )
        c.context {
            helpFormatter = {
                PlaintextHelpFormatter(
                    it,
                    showDefaultValues = true,
                    requiredOptionMarker = "*",
                    showRequiredTag = true
                )
            }
        }
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  aa option help
            |* -b, --bb=<text>  bb option help (t1) (t2: v2) (required)
            """
        )
    }

    @Test
    @JsName("argument_tag")
    fun `argument tag`() {
        c.registerArgument(c.argument("ARG1", help = "arg 1 help"))
        c.registerArgument(
            c.argument(
                "ARG2",
                help = "arg 2 help",
                helpTags = mapOf("deprecated" to "")
            )
        )
        doTest(
            """
            |Usage: prog <arg1> <arg2>
            |
            |Arguments:
            |  <arg1>  arg 1 help
            |  <arg2>  arg 2 help (deprecated)
            """
        )
    }

    @Test
    @JsName("subcommand_tag")
    fun `subcommand tag`() {
        c.subcommands(
            TestCommand(name = "sub1", help = "sub 1 help"),
            TestCommand(name = "sub2", help = "sub 2 help", helpTags = mapOf("deprecated" to "")),
        )
        doTest(
            """
            |Usage: prog <command> [<args>]...
            |
            |Commands:
            |  sub1  sub 1 help
            |  sub2  sub 2 help (deprecated)
            """
        )
    }


    @Test
    @JsName("multi_error")
    fun `multi error`() {
        c.test("--foo --bar").stderr shouldBe """
            |Usage: prog [<options>]
            |
            |Error: no such option --foo
            |Error: no such option --bar
            |
            """.trimMargin()
    }
}
