package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import io.kotlintest.shouldBe
import org.junit.Test

private fun <T> l(vararg t: T) = listOf(*t)

private fun opt(
        names: List<String>,
        metavar: String? = null,
        help: String = "",
        nvalues: Int = 1,
        secondaryNames: List<String> = emptyList(),
        tags: Map<String, String> = emptyMap()
): ParameterHelp.Option {
    return ParameterHelp.Option(names.toSet(), secondaryNames.toSet(), metavar, help, nvalues, tags)
}

private fun opt(
        name: String,
        metavar: String? = null,
        help: String = "",
        nvalues: Int = 1,
        secondaryNames: List<String> = emptyList(),
        tags: Map<String, String> = emptyMap()
): ParameterHelp.Option {
    return opt(l(name), metavar, help, nvalues, secondaryNames, tags)
}

private fun arg(
        name: String,
        help: String = "",
        required: Boolean = false,
        repeatable: Boolean = false,
        tags: Map<String, String> = emptyMap()
) = ParameterHelp.Argument(name, help, required, repeatable, tags)

private fun sub(
        name: String,
        help: String = "",
        tags: Map<String, String> = emptyMap()
) = ParameterHelp.Subcommand(name, help, tags)

class PlaintextHelpFormatterTest {
    @Test
    fun formatUsage() {
        val f = PlaintextHelpFormatter()
        f.formatUsage(l(), programName = "prog1") shouldBe "Usage: prog1"
        f.formatUsage(l(opt("-x")), programName = "prog2") shouldBe "Usage: prog2 [OPTIONS]"
        f.formatUsage(l(arg("FOO")), programName = "prog3") shouldBe "Usage: prog3 [FOO]"
        f.formatUsage(l(arg("FOO", required = true)), programName = "prog4") shouldBe "Usage: prog4 FOO"
        f.formatUsage(l(arg("FOO", repeatable = true)), programName = "prog5") shouldBe "Usage: prog5 [FOO]..."
        f.formatUsage(l(arg("FOO", required = true, repeatable = true)), programName = "prog6") shouldBe "Usage: prog6 FOO..."
        f.formatUsage(l(
                arg("FOO", required = true, repeatable = true), opt("-x"), arg("BAR")), programName = "prog7"
        ) shouldBe "Usage: prog7 [OPTIONS] FOO... [BAR]"
        f.formatUsage(l(opt("-x"), arg("FOO"), sub("bar")), programName = "prog8"
        ) shouldBe "Usage: prog8 [OPTIONS] [FOO] COMMAND [ARGS]..."
    }

    @Test
    fun `formatUsage wrapping options string`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatUsage(l(
                opt("-x"),
                arg("FIRST", required = true),
                arg("SECOND", required = true),
                arg("THIRD", required = true),
                arg("FOURTH", required = true),
                arg("FIFTH", required = true),
                arg("SIXTH", required = true)
        ), programName = "cli a_very_long command") shouldBe
                """
                |Usage: cli a_very_long command [OPTIONS] FIRST SECOND
                |                               THIRD FOURTH FIFTH
                |                               SIXTH
                """.trimMargin()
    }

    @Test
    fun `formatUsage wrapping command name`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatUsage(l(
                opt("-x"),
                arg("FIRST", required = true),
                arg("SECOND", required = true),
                arg("THIRD", required = true),
                arg("FOURTH", required = true),
                arg("FIFTH", required = true),
                arg("SIXTH", required = true)
        ), programName = "cli a_very_very_very_long command") shouldBe
                """
                |Usage: cli a_very_very_very_long command
                |           [OPTIONS] FIRST SECOND THIRD FOURTH FIFTH
                |           SIXTH
                """.trimMargin()
    }

    @Test
    fun `formatHelp one opt`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp("", "", l(opt(l("--aa", "-a"), "INT", "some thing to live by")),
                programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp one opt secondary name`() {
        val f = PlaintextHelpFormatter(width = 60)
        f.formatHelp("", "", l(
                opt(l("--aa", "-a"), null, "some thing to know", secondaryNames = listOf("--no-aa", "-A"))
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa / -A, --no-aa  some thing to know
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp one opt prolog`() {
        val f = PlaintextHelpFormatter()
        f.formatHelp(prolog = "Lorem Ipsum.", epilog = "Dolor Sit Amet.",
                parameters = l(opt(l("--aa", "-a"), "INT", "some thing to live by")),
                programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |  Lorem Ipsum.
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                |
                |Dolor Sit Amet.
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp one opt prolog multi paragraph`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp(prolog = """Lorem ipsum dolor sit amet, consectetur adipiscing elit.

                Vivamus dictum varius massa, at euismod turpis maximus eu. Suspendisse molestie mauris at
                turpis bibendum egestas.

                Morbi id libero purus. Praesent sit amet neque tellus. Vestibulum in condimentum turpis, in
                consectetur ex.
                """, epilog = "",
                parameters = l(opt(l("--aa", "-a"), "INT", "some thing to live by")),
                programName = "prog") shouldBe
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
                |  -a, --aa INT  some thing to live by
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp option wrapping`() {
        val f = PlaintextHelpFormatter(width = 54, maxColWidth = 12)
        f.formatHelp("", "", l(
                opt(l("-x"), "X", nvalues = 2, help = "one very very very very very very long option"),
                opt(l("-y", "--yy"), "Y", help = "a shorter but still long option"),
                opt(l("-z", "--zzzzzzzzzzzzz"), "ZZZZZZZZ", help = "a short option"),
                opt(l("-t", "--entirely-too-long-option"), "WOWSOLONG",
                        help = "this option has a long name and a long descrption")
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -x X...       one very very very very very very long
                |                option
                |  -y, --yy Y    a shorter but still long option
                |  -z, --zzzzzzzzzzzzz ZZZZZZZZ
                |                a short option
                |  -t, --entirely-too-long-option WOWSOLONG
                |                this option has a long name and a long
                |                descrption
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp option wrapping long help issue #10`() {
        val f = PlaintextHelpFormatter(width = 62)
        f.formatHelp("", "", l(
                opt(l("-L", "--lorem-ipsum"),
                        help = "Lorem ipsum dolor sit amet, consectetur e  adipiscing elit. Nulla vitae " +
                                "porta nisi.  Interdum et malesuada fames ac ante ipsum")
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -L, --lorem-ipsum  Lorem ipsum dolor sit amet, consectetur e
                |                     adipiscing elit. Nulla vitae porta nisi.
                |                     Interdum et malesuada fames ac ante ipsum
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp arguments`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp("", "", l(
                arg("FOO", "some thing to live by", required = true),
                arg("BAR", "another argument")
        ), programName = "prog") shouldBe
                """
                |Usage: prog FOO [BAR]
                |
                |Arguments:
                |  FOO  some thing to live by
                |  BAR  another argument
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp subcommands`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp("", "", l(
                sub("foo", "some thing to live by"),
                sub("bar", "another argument")),
                programName = "prog") shouldBe
                """
                |Usage: prog COMMAND [ARGS]...
                |
                |Commands:
                |  foo  some thing to live by
                |  bar  another argument
                """.trimMargin("|")
    }

    @Test
    fun `integration test`() {
        @Suppress("unused")
        class C : NoRunCliktCommand(name = "program",
                help = """
                This is a program.

                This is the prolog.
                """,
                epilog = "This is the epilog") {
            val foo by option(help = "foo option help").int().required()
            val bar by option("-b", "--bar", help = "bar option help", metavar = "META").default("optdef")
            val baz by option(help = "baz option help").flag("--no-baz")
            val hidden by option(help = "hidden", hidden = true)
            val arg by argument()
            val multi by argument().multiple(required = true)

            init {
                context {
                    helpFormatter = PlaintextHelpFormatter(
                            showDefaultValues = true,
                            showRequiredTag = true,
                            requiredOptionMarker = "*"
                    )
                }
            }
        }

        class Sub : CliktCommand(help = """
            a subcommand

            with extra help
            """,
                helpTags = mapOf("deprecated" to "")) {
            override fun run() = Unit
        }

        class Sub2 : CliktCommand(help = "another command") {
            override fun run() = Unit
        }

        val c = C()
                .versionOption("1.0")
                .subcommands(Sub(), Sub2())

        c.getFormattedHelp() shouldBe
                """
                |Usage: program [OPTIONS] ARG [MULTI]... COMMAND [ARGS]...
                |
                |  This is a program.
                |
                |  This is the prolog.
                |
                |Options:
                |* --foo INT         foo option help (required)
                |  -b, --bar META    bar option help (default: optdef)
                |  --baz / --no-baz  baz option help
                |  --version         Show the version and exit
                |  -h, --help        Show this message and exit
                |
                |Commands:
                |  sub   a subcommand (deprecated)
                |  sub2  another command
                |
                |This is the epilog
                """.trimMargin("|")
    }

    @Test
    fun `required option marker`() {
        val f = PlaintextHelpFormatter(width = 54, requiredOptionMarker = "*")
        f.formatHelp("", "", l(
                opt(l("--aa", "-a"), "INT", "aa option help"),
                opt(l("--bb", "-b"), "INT", "bb option help", tags = mapOf(HelpFormatter.Tags.REQUIRED to ""))
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  aa option help
                |* -b, --bb INT  bb option help
                """.trimMargin("|")
    }

    @Test
    fun `required option tag`() {
        val f = PlaintextHelpFormatter(width = 54, showRequiredTag = true)
        f.formatHelp("", "", l(
                opt(l("--aa", "-a"), "INT", "aa option help"),
                opt(l("--bb", "-b"), "INT", "bb option help", tags = mapOf(HelpFormatter.Tags.REQUIRED to ""))
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  aa option help
                |  -b, --bb INT  bb option help (required)
                """.trimMargin("|")
    }

    @Test
    fun `default option tag`() {
        val f = PlaintextHelpFormatter(width = 54, showDefaultValues = true)
        f.formatHelp("", "", l(
                opt(l("--aa", "-a"), "INT", "aa option help"),
                opt(l("--bb", "-b"), "INT", "bb option help", tags = mapOf(HelpFormatter.Tags.DEFAULT to "123"))
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  aa option help
                |  -b, --bb INT  bb option help (default: 123)
                """.trimMargin("|")
    }

    @Test
    fun `custom tag`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp("", "", l(
                opt(l("--aa", "-a"), "INT", "aa option help"),
                opt(l("--bb", "-b"), "INT", "bb option help", tags = mapOf("deprecated" to ""))
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  aa option help
                |  -b, --bb INT  bb option help (deprecated)
                """.trimMargin("|")
    }

    @Test
    fun `argument tag`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp("", "", l(
                arg("ARG1", "arg 1 help"),
                arg("ARG2", "arg 2 help", tags = mapOf("deprecated" to ""))
        ), programName = "prog") shouldBe
                """
                |Usage: prog [ARG1] [ARG2]
                |
                |Arguments:
                |  ARG1  arg 1 help
                |  ARG2  arg 2 help (deprecated)
                """.trimMargin("|")
    }

    @Test
    fun `subcommand tag`() {
        val f = PlaintextHelpFormatter(width = 54)
        f.formatHelp("", "", l(
                sub("sub1", "sub 1 help"),
                sub("sub2", "sub 2 help", tags = mapOf("deprecated" to ""))
        ), programName = "prog") shouldBe
                """
                |Usage: prog COMMAND [ARGS]...
                |
                |Commands:
                |  sub1  sub 1 help
                |  sub2  sub 2 help (deprecated)
                """.trimMargin("|")
    }
}
