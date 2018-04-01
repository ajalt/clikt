package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.softly
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private fun <T> l(vararg t: T) = listOf(*t)

private fun opt(names: List<String>,
                metavar: String? = null,
                help: String = "",
                nvalues: Int = 1,
                secondaryNames: List<String> = emptyList()): ParameterHelp.Option {
    return ParameterHelp.Option(names.toSet(), secondaryNames.toSet(), metavar, help, nvalues)
}

private fun opt(name: String,
                metavar: String? = null,
                help: String = "",
                nvalues: Int = 1,
                secondaryNames: List<String> = emptyList()): ParameterHelp.Option {
    return opt(l(name), metavar, help, nvalues, secondaryNames)
}

private fun arg(name: String,
                help: String = "",
                required: Boolean = false,
                repeatable: Boolean = false) = ParameterHelp.Argument(name, help, required, repeatable)

private fun sub(name: String,
                help: String = "") = ParameterHelp.Subcommand(name, help)

class PlaintextHelpFormatterTest {
    @Test
    fun formatUsage() {
        val f = PlaintextHelpFormatter()
        softly {
            assertThat(f.formatUsage(l(), programName = "prog1")).isEqualTo("Usage: prog1")
            assertThat(f.formatUsage(l(opt("-x")), programName = "prog2")).isEqualTo(
                    "Usage: prog2 [OPTIONS]")
            assertThat(f.formatUsage(l(arg("FOO")), programName = "prog3")).isEqualTo(
                    "Usage: prog3 [FOO]")
            assertThat(f.formatUsage(l(arg("FOO", required = true)), programName = "prog4")).isEqualTo(
                    "Usage: prog4 FOO")
            assertThat(f.formatUsage(l(arg("FOO", repeatable = true)), programName = "prog5")).isEqualTo(
                    "Usage: prog5 [FOO]...")
            assertThat(f.formatUsage(l(arg("FOO", required = true, repeatable = true)), programName = "prog6")).isEqualTo(
                    "Usage: prog6 FOO...")
            assertThat(f.formatUsage(l(
                    arg("FOO", required = true, repeatable = true),
                    opt("-x"),
                    arg("BAR")), programName = "prog7")).isEqualTo(
                    "Usage: prog7 [OPTIONS] FOO... [BAR]")
            assertThat(f.formatUsage(l(
                    opt("-x"),
                    arg("FOO"),
                    sub("bar")), programName = "prog8")).isEqualTo(
                    "Usage: prog8 [OPTIONS] [FOO] COMMAND [ARGS]...")
        }
    }

    @Test
    fun `formatUsage wrapping options string`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatUsage(l(
                opt("-x"),
                arg("FIRST", required = true),
                arg("SECOND", required = true),
                arg("THIRD", required = true),
                arg("FOURTH", required = true),
                arg("FIFTH", required = true),
                arg("SIXTH", required = true)
        ), programName = "cli a_very_long command")).isEqualTo(
                """
                |Usage: cli a_very_long command [OPTIONS] FIRST SECOND
                |                               THIRD FOURTH FIFTH
                |                               SIXTH
                """.trimMargin())
    }

    @Test
    fun `formatUsage wrapping command name`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatUsage(l(
                opt("-x"),
                arg("FIRST", required = true),
                arg("SECOND", required = true),
                arg("THIRD", required = true),
                arg("FOURTH", required = true),
                arg("FIFTH", required = true),
                arg("SIXTH", required = true)
        ), programName = "cli a_very_very_very_long command")).isEqualTo(
                """
                |Usage: cli a_very_very_very_long command
                |           [OPTIONS] FIRST SECOND THIRD FOURTH FIFTH
                |           SIXTH
                """.trimMargin())
    }

    @Test
    fun `formatHelp one opt`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatHelp("", "", l(opt(l("--aa", "-a"), "INT", "some thing to live by")),
                programName = "prog")).isEqualTo(
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp one opt secondary name`() {
        val f = PlaintextHelpFormatter(width = 60)
        assertThat(f.formatHelp("", "", l(opt(l("--aa", "-a"),
                null, "some thing to know", secondaryNames = listOf("--no-aa", "-A"))),
                programName = "prog")).isEqualTo(
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa / -A, --no-aa  some thing to know
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp one opt prolog`() {
        val f = PlaintextHelpFormatter()
        assertThat(f.formatHelp(prolog = "Lorem Ipsum.", epilog = "Dolor Sit Amet.",
                parameters = l(opt(l("--aa", "-a"), "INT", "some thing to live by")),
                programName = "prog")).isEqualTo(
                """
                |Usage: prog [OPTIONS]
                |
                |  Lorem Ipsum.
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                |
                |Dolor Sit Amet.
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp one opt prolog multi paragraph`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatHelp(prolog = """Lorem ipsum dolor sit amet, consectetur adipiscing elit.

                Vivamus dictum varius massa, at euismod turpis maximus eu. Suspendisse molestie mauris at
                turpis bibendum egestas.

                Morbi id libero purus. Praesent sit amet neque tellus. Vestibulum in condimentum turpis, in
                consectetur ex.
                """, epilog = "",
                parameters = l(opt(l("--aa", "-a"), "INT", "some thing to live by")),
                programName = "prog")).isEqualTo(
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
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp option wrapping`() {
        val f = PlaintextHelpFormatter(width = 54, maxColWidth = 12)
        assertThat(f.formatHelp("", "", l(
                opt(l("-x"), "X", nvalues = 2, help = "one very very very very very very long option"),
                opt(l("-y", "--yy"), "Y", help = "a shorter but still long option"),
                opt(l("-z", "--zzzzzzzzzzzzz"), "ZZZZZZZZ", help = "a short option"),
                opt(l("-t", "--entirely-too-long-option"), "WOWSOLONG",
                        help = "this option has a long name and a long descrption")
        ), programName = "prog")).isEqualTo(
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
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp arguments`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatHelp("", "", l(
                arg("FOO", "some thing to live by", required = true),
                arg("BAR", "another argument")),
                programName = "prog")).isEqualTo(
                """
                |Usage: prog FOO [BAR]
                |
                |Arguments:
                |  FOO  some thing to live by
                |  BAR  another argument
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp subcommands`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatHelp("", "", l(
                sub("foo", "some thing to live by"),
                sub("bar", "another argument")),
                programName = "prog")).isEqualTo(
                """
                |Usage: prog COMMAND [ARGS]...
                |
                |Commands:
                |  foo  some thing to live by
                |  bar  another argument
                """.trimMargin("|"))
    }

    @Test
    fun `integration test`() {
        @Suppress("unused")
        class C : CliktCommand(name = "program",
                help ="""
                This is a program.

                This is the prolog.
                """,
                epilog = "This is the epilog") {
            val foo by option(help = "foo option help").int()
            val bar by option("-b", "--bar", help = "bar option help", metavar = "META")
            val baz by option(help = "baz option help").flag("--no-baz")
            val hidden by option(help = "hidden", hidden = true)
            val arg by argument()
            val multi by argument().multiple(required = true)

            override fun run() = Unit
        }

        class Sub : CliktCommand(help = """
            a subcommand

            with extra help
            """) {
            override fun run() = Unit
        }

        class Sub2 : CliktCommand(help = "another command") {
            override fun run() = Unit
        }

        val c = C()
                .versionOption("1.0")
                .subcommands(Sub(), Sub2())

        assertThat(c.getFormattedHelp()).isEqualTo(
                """
                |Usage: program [OPTIONS] ARG [MULTI]... COMMAND [ARGS]...
                |
                |  This is a program.
                |
                |  This is the prolog.
                |
                |Options:
                |  --foo INT         foo option help
                |  -b, --bar META    bar option help
                |  --baz / --no-baz  baz option help
                |  --version         Show the version and exit.
                |  -h, --help        Show this message and exit
                |
                |Commands:
                |  sub   a subcommand
                |  sub2  another command
                |
                |This is the epilog
                """.trimMargin("|"))
    }
}
