package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.groups.*
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.test
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test


class MordantHelpFormatterTest {
    private val c = TestCommand(name = "prog")
    private fun doTest(
        expected: String,
        width: Int = 79,
        command: CliktCommand = c,
        helpNames: Set<String> = emptySet(),
    ) {
        command.context {
            terminal = Terminal(width = width, ansiLevel = AnsiLevel.NONE)
            helpOptionNames = helpNames
        }.getFormattedHelp() shouldBe expected.trimMargin()
    }

    @Test
    fun formatUsage() = forAll(
        row(listOf(), "Usage: prog"),
        row(listOf(c.option("-x")), "Usage: prog [OPTIONS]"),
        row(listOf(c.argument("FOO").optional()), "Usage: prog [FOO]"),
        row(listOf(c.argument("FOO")), "Usage: prog FOO"),
        row(listOf(c.argument("FOO").multiple()), "Usage: prog [FOO]..."),
        row(listOf(c.argument("FOO").multiple(required = true)), "Usage: prog FOO..."),
        row(
            listOf<Any>(c.argument("FOO").multiple(required = true), c.option("-x"), c.argument("BAR").optional()),
            "Usage: prog [OPTIONS] FOO... [BAR]"
        ),
        row(
            listOf(c.option("-x"), c.argument("FOO").optional(), TestCommand(name = "bar")),
            "Usage: prog [OPTIONS] [FOO] COMMAND [ARGS]..."
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
            }
        }
        c.getFormattedHelp()?.lines()?.first() shouldBe expected
    }

    @Test
    @Suppress("unused")
    @JsName("formatUsage_wrapping_command_name")
    fun `formatUsage wrapping command name`() {
        class C : TestCommand(name = "cli a_very_very_very_long command") {
            val x by option("-x")
            val a1 by argument("FIRST")
            val a2 by argument("SECOND")
            val a3 by argument("THIRD")
            val a4 by argument("FOURTH")
            val a5 by argument("FIFTH")
            val a6 by argument("SIXTH")
        }
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by"))
        doTest(
            """
            |Usage: cli a_very_very_very_long command
            |    [OPTIONS] FIRST SECOND THIRD FOURTH FIFTH
            |    SIXTH
            |    
            |Options:
            |  -x=TEXT
            """,
            width = 46,
            command = C()
        )
    }

    @Test
    @JsName("formatUsage_narrow_width")
    fun `formatUsage narrow width`() {
        c.registerOption(c.option("-x"))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -x=TEXT
            """,
            width = 21,
        )
    }


    @Test
    @JsName("help_output_one_opt")
    fun `one opt`() {
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by"))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  some thing to live by
            """
        )
    }

    @Test
    @JsName("help_output_number_opt")
    fun `number opt`() {
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by").int(acceptsValueWithoutName = true))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -INT, -a, --aa=INT  some thing to live by
            """
        )
    }

    @Test
    @JsName("help_output_one_opt_secondary_name")
    fun `one opt secondary name`() {
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by").flag("--no-aa", "-A"))
        doTest(
            """
            |Usage: prog [OPTIONS]
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
            |Usage: prog [OPTIONS]
            |
            |  Lorem Ipsum.
            |
            |Options:
            |  -a, --aa=INT  some thing to live by
            |
            |Dolor Sit Amet.
            """,
            command = c
        )
    }

    @Test
    @JsName("help_output_one_opt_prolog_multi_paragraph")
    fun `one opt prolog multi paragraph`() {
        val c = TestCommand(
            name = "prog",
            help = """
            |Lorem ipsum dolor sit amet, consectetur adipiscing elit.
            |
            |Vivamus dictum varius massa, at euismod turpis maximus eu. Suspendisse molestie mauris at
            |turpis bibendum egestas.
            |
            |Morbi id libero purus. Praesent sit amet neque tellus. Vestibulum in condimentum turpis, in
            |consectetur ex.
            """.trimMargin(),
        )
        c.registerOption(c.option("--aa", "-a", help = "some thing to live by").int())
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |  Lorem ipsum dolor sit amet, consectetur adipiscing
            |  elit.
            |
            |  Vivamus dictum varius massa, at euismod turpis
            |  maximus eu. Suspendisse molestie mauris at turpis
            |  bibendum egestas.
            |
            |  Morbi id libero purus. Praesent sit amet neque
            |  tellus. Vestibulum in condimentum turpis, in
            |  consectetur ex.
            |
            |Options:
            |  -a, --aa=INT  some thing to live by
            """,
            width = 54,
            command = c
        )
    }

    @Test
    @JsName("help_output_prolog_list")
    fun `prolog list`() {
        val c = TestCommand(
            name = "prog",
            help = """
            |Lorem ipsum dolor sit amet, consectetur adipiscing elit.
            |
            |- Morbi id libero purus.
            |- Praesent sit amet neque tellus.
            |
            |Vivamus dictum varius massa, at euismod turpis maximus eu. Suspendisse molestie mauris at
            |turpis bibendum egestas.
            """.trimMargin(),
        )
        doTest(
            """
            |Usage: prog
            |
            |  Lorem ipsum dolor sit amet, consectetur adipiscing
            |  elit.
            |
            |   • Morbi id libero purus.
            |   • Praesent sit amet neque tellus.
            |
            |  Vivamus dictum varius massa, at euismod turpis
            |  maximus eu. Suspendisse molestie mauris at turpis
            |  bibendum egestas.
            """,
            width = 54,
            command = c
        )
    }

    @Test
    @JsName("help_output_one_opt_manual_line_break_narrow")
    fun `one opt manual line break narrow`() {
        c.registerOption(c.option("--aa", "-a", help = "Lorem ipsum dolor\u0085(sit amet, consectetur)"))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT
            |    Lorem ipsum dolor
            |    (sit amet, consectetur)
            """,
            width = 35
        )
    }

    @Test
    @JsName("help_output_one_opt_manual_line_break_wide")
    fun `one opt manual line break wide`() {
        c.registerOption(c.option("--aa", "-a", help = "Lorem ipsum dolor\u0085(sit amet, consectetur)"))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  Lorem ipsum dolor
            |                 (sit amet, consectetur)
            """
        )
    }

    @Test
    @JsName("help_output_option_wrapping")
    fun `option wrapping`() {
        c.registerOption(
            c.option("-x", metavar = "X", help = "one very very very very very very long option").pair()
        )
        c.registerOption(c.option("-y", "--yy", metavar = "Y", help = "a shorter but still long option"))
        c.registerOption(c.option("-z", "--zzzzzzzzzzzzz", metavar = "ZZZZZZZZ", help = "a short option"))
        c.registerOption(
            c.option(
                "-t", "--entirely-too-long-option", metavar = "WOWSOLONG",
                help = "this option has a long name and a long description"
            )
        )
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -x=X...     one very very very very very very long
            |              option
            |  -y, --yy=Y  a shorter but still long option
            |  -z, --zzzzzzzzzzzzz=ZZZZZZZZ
            |              a short option
            |  -t, --entirely-too-long-option=WOWSOLONG
            |              this option has a long name and a long
            |              description
            """,
            width = 54
        )
    }

    @Test
    @JsName("help_output_option_wrapping_long_help_issue_10")
    fun `option wrapping long help issue 10`() {
        c.registerOption(
            c.option("-L", "--lorem-ipsum")
                .flag()
                .help(
                    "Lorem ipsum dolor sit amet, consectetur e  adipiscing elit. Nulla vitae porta nisi." +
                            " Interdum et malesuada fames ac ante ipsum"
                )
        )
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -L, --lorem-ipsum  Lorem ipsum dolor sit amet, consectetur e
            |                     adipiscing elit. Nulla vitae porta nisi.
            |                     Interdum et malesuada fames ac ante ipsum
            """,
            width = 62
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
            |Usage: prog [OPTIONS]
            |
            |Grouped:
            |
            |  This is the help text for the option group named
            |  Grouped. This text should wrap onto exactly three
            |  lines.
            |
            |  -a, --aa=TEXT  some thing to live by aa
            |  -c, --cc=TEXT  some thing to live by cc
            |
            |Singleton:
            |  -b, --bb=TEXT  some thing to live by bb
            |
            |Options:
            |  -d, --dd=TEXT  some thing to live by dd
            """,
            width = 54,
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
            |Usage: prog FOO [BAR]
            |
            |Arguments:
            |  FOO  some thing to live by
            |  BAR  another argument
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
            TestCommand(name = "qux", hidden = true),
        )
        doTest(
            """
            |Usage: prog COMMAND [ARGS]...
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
        c.registerOption(c.option("--bar", help = "option two").choice("c1", "c2").optionalValue("d2"))
        c.registerOption(c.option("--baz", help = "option three").int().varargValues(min = 0))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -f, --foo[=TEXT]  option one
            |  --bar[=c1|c2]     option two
            |  --baz[=INT...]    option three
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
            |Usage: c [OPTIONS]
            |
            |G1:
            |  --opt1=TEXT
            |
            |G2:
            |  --opt2=TEXT
            |
            |Options:
            |  --opt=[g1|g2]  select group
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
            |Usage: c [OPTIONS]
            |
            |G1:
            |  --opt1=TEXT
            |
            |G2:
            |  --opt2=TEXT
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
            |Usage: prog [OPTIONS]
            |
            |Exclusive:
            |
            |  These options are exclusive
            |
            |  --ex-foo=TEXT  exclusive foo
            |  --ex-bar=TEXT  exclusive bar
            |
            |Exclusive without help:
            |  --ex-baz=TEXT  exclusive baz
            |  --ex-qux=TEXT  exclusive qux
            |  --ex-quz=TEXT  exclusive quz
            """,
            command = C()
        )
    }

    @Test
    @JsName("eager_options")
    fun `eager options`() {
        c.versionOption("1.0")
        c.eagerOption("--eager", "-e", help = "this is an eager option with a group", groupName = "My Group") {}
        c.eagerOption("--eager2", "-E", help = "this is an eager option") {}
        doTest(
            """
            |Usage: prog [OPTIONS]
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
        c.context { helpFormatter = MordantHelpFormatter(requiredOptionMarker = "*") }
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  aa option help
            |* -b, --bb=TEXT  bb option help
            """
        )
    }

    @Test
    @JsName("required_option_tag")
    fun `required option tag`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(c.option("--bb", "-b", help = "bb option help").required())
        c.context { helpFormatter = MordantHelpFormatter(showRequiredTag = true) }
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  aa option help
            |  -b, --bb=TEXT  bb option help (required)
            """
        )
    }

    @Test
    @JsName("default_option_tag")
    fun `default option tag`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(c.option("--bb", "-b", help = "bb option help").default("123"))
        c.context { helpFormatter = MordantHelpFormatter(showDefaultValues = true) }
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  aa option help
            |  -b, --bb=TEXT  bb option help (default: 123)
            """
        )
    }

    @Test
    @JsName("custom_tag")
    fun `custom tag`() {
        c.registerOption(c.option("--aa", "-a", help = "aa option help"))
        c.registerOption(c.option("--bb", "-b", help = "bb option help", helpTags = mapOf("deprecated" to "")))
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  aa option help
            |  -b, --bb=TEXT  bb option help (deprecated)
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
            helpFormatter = MordantHelpFormatter(
                showDefaultValues = true,
                requiredOptionMarker = "*",
                showRequiredTag = true
            )
        }
        doTest(
            """
            |Usage: prog [OPTIONS]
            |
            |Options:
            |  -a, --aa=TEXT  aa option help
            |* -b, --bb=TEXT  bb option help (t1) (t2: v2) (required)
            """
        )
    }

    @Test
    @JsName("argument_tag")
    fun `argument tag`() {
        c.registerArgument(c.argument("ARG1", help = "arg 1 help"))
        c.registerArgument(c.argument("ARG2", help = "arg 2 help", helpTags = mapOf("deprecated" to "")))
        doTest(
            """
            |Usage: prog ARG1 ARG2
            |
            |Arguments:
            |  ARG1  arg 1 help
            |  ARG2  arg 2 help (deprecated)
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
            |Usage: prog COMMAND [ARGS]...
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
        TestCommand().test("--foo --bar").stderr shouldBe """
            |Usage: test [OPTIONS]
            |
            |Error: no such option: "--foo"
            |Error: no such option: "--bar"
            |
            """.trimMargin()
    }
}
