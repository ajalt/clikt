package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.testing.softly
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val OPT = HelpFormatter.ParameterHelp.Companion.SECTION_OPTIONS
private const val ARG = HelpFormatter.ParameterHelp.Companion.SECTION_ARGUMENTS
private const val CMD = HelpFormatter.ParameterHelp.Companion.SECTION_SUBCOMMANDS

private fun <T> l(vararg t: T) = listOf(*t)
private fun h(names: List<String>,
              section: Int,
              metavar: String? = null,
              help: String = "",
              required: Boolean = false,
              repeatable: Boolean = false) = ParameterHelp(names,
        metavar,
        help,
        section,
        required,
        repeatable)

class PlaintextHelpFormatterTest {

    @Test
    fun formatUsage() {
        val f = PlaintextHelpFormatter()
        softly {
            assertThat(f.formatUsage(l(), programName = "prog1")).isEqualTo("Usage: prog1")
            assertThat(f.formatUsage(l(h(l("-x"), OPT)), programName = "prog2")).isEqualTo(
                    "Usage: prog2 [OPTIONS]")
            assertThat(f.formatUsage(l(h(l("foo"), ARG, "FOO")), programName = "prog3")).isEqualTo(
                    "Usage: prog3 [FOO]")
            assertThat(f.formatUsage(l(h(l("foo"), ARG, "FOO", required = true)), programName = "prog4")).isEqualTo(
                    "Usage: prog4 FOO")
            assertThat(f.formatUsage(l(h(l("foo"), ARG, "FOO", repeatable = true)), programName = "prog5")).isEqualTo(
                    "Usage: prog5 [FOO]...")
            assertThat(f.formatUsage(l(h(l("foo"), ARG, "FOO", required = true, repeatable = true)), programName = "prog6")).isEqualTo(
                    "Usage: prog6 FOO...")
            assertThat(f.formatUsage(l(
                    h(l("foo"), ARG, "FOO", required = true, repeatable = true),
                    h(l("-x"), OPT),
                    h(l("bar"), ARG, "BAR")), programName = "prog7")).isEqualTo(
                    "Usage: prog7 [OPTIONS] FOO... [BAR]")
            assertThat(f.formatUsage(l(
                    h(l("-x"), OPT),
                    h(l("foo"), ARG, "FOO"),
                    h(l("bar"), CMD, "BAR")), programName = "prog8")).isEqualTo(
                    "Usage: prog8 [OPTIONS] [FOO] COMMAND [ARGS]...")
        }
    }

    @Test
    fun `formatUsage wrapping options string`() {
        val f = PlaintextHelpFormatter(width = 54)
        assertThat(f.formatUsage(l(
                h(l("-x"), OPT),
                h(l(), ARG, "FIRST", required = true),
                h(l(), ARG, "SECOND", required = true),
                h(l(), ARG, "THIRD", required = true),
                h(l(), ARG, "FOURTH", required = true),
                h(l(), ARG, "FIFTH", required = true),
                h(l(), ARG, "SIXTH", required = true)
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
                h(l("-x"), OPT),
                h(l(), ARG, "FIRST", required = true),
                h(l(), ARG, "SECOND", required = true),
                h(l(), ARG, "THIRD", required = true),
                h(l(), ARG, "FOURTH", required = true),
                h(l(), ARG, "FIFTH", required = true),
                h(l(), ARG, "SIXTH", required = true)
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
        assertThat(f.formatHelp(l(h(listOf("--aa", "-a"), OPT,
                "INT", "some thing to live by", false, false)), programName = "prog")).isEqualTo(
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
        assertThat(f.formatHelp(l(h(listOf("--aa", "-a"), OPT,
                "INT", "some thing to live by", false, false)), programName = "prog")).isEqualTo(
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
}
