package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.testing.softly
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private fun <T> l(vararg t: T) = listOf(*t)

private fun opt(names: List<String>,
                metavar: String? = null,
                help: String = "",
                repeatable: Boolean = false) = ParameterHelp.Option(names, metavar, help, repeatable)

private fun opt(name: String,
                metavar: String? = null,
                help: String = "",
                repeatable: Boolean = false) = opt(l(name), metavar, help, repeatable)

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
        assertThat(f.formatHelp(l(opt(l("--aa", "-a"),
                "INT", "some thing to live by", false)), programName = "prog")).isEqualTo(
                """
                |Usage: prog [OPTIONS]
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp one opt prolog`() {
        val f = PlaintextHelpFormatter(prolog = "Lorem Ipsum.", epilog = "Dolor Sit Amet.")
        assertThat(f.formatHelp(l(opt(l("--aa", "-a"),
                "INT", "some thing to live by", false)), programName = "prog")).isEqualTo(
                """
                |Usage: prog [OPTIONS]
                |
                |Lorem Ipsum.
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                |
                |Dolor Sit Amet.
                """.trimMargin("|"))
    }

    @Test
    fun `formatHelp option wrapping`() {
        val f = PlaintextHelpFormatter(width = 54, maxColWidth = 12)
        assertThat(f.formatHelp(l(
                opt(l("-x"), "X", repeatable = true, help = "one very very very very very very long option"),
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
}
