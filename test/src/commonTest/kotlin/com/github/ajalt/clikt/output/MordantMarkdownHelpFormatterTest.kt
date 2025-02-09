package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.installMordantMarkdown
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.js.JsName
import kotlin.test.Test


@Suppress("unused")
class MordantMarkdownHelpFormatterTest {
    private val c = TestCommand(name = "prog")
    private fun doTest(
        expected: String,
        width: Int = 79,
        command: CliktCommand = c,
        helpNames: Set<String> = emptySet(),
    ) {
        command.installMordantMarkdown()
        val formattedHelp = command.context {
            terminal = Terminal(width = width, ansiLevel = AnsiLevel.NONE)
            helpOptionNames = helpNames
        }.getFormattedHelp()
        command.currentContext.helpFormatter(command.currentContext)
            .shouldBeInstanceOf<MordantMarkdownHelpFormatter>()
        formattedHelp shouldBe expected.trimMargin()
    }

    @[Test JsName("help_output_one_opt_prolog_multi_paragraph")]
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
            |Usage: prog [<options>]
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
            |  -a, --aa=<int>  some thing to live by
            """,
            width = 54,
            command = c
        )
    }

    @[Test JsName("help_output_prolog_list")]
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

    @[Test JsName("help_output_one_opt_manual_line_break_narrow")]
    fun `one opt manual line break narrow`() {
        c.registerOption(
            c.option(
                "--aa",
                "-a",
                help = "Lorem ipsum dolor\u0085(sit amet, consectetur)"
            )
        )
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>
            |    Lorem ipsum dolor
            |    (sit amet, consectetur)
            """,
            width = 35
        )
    }

    @[Test JsName("help_output_one_opt_manual_line_break_wide")]
    fun `one opt manual line break wide`() {
        c.registerOption(
            c.option(
                "--aa",
                "-a",
                help = "Lorem ipsum dolor\u0085(sit amet, consectetur)"
            )
        )
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -a, --aa=<text>  Lorem ipsum dolor
            |                   (sit amet, consectetur)
            """
        )
    }

    @[Test JsName("help_output_option_wrapping")]
    fun `option wrapping`() {
        c.registerOption(
            c.option("-x", metavar = "X", help = "one very very very very very very long option")
                .pair()
        )
        c.registerOption(
            c.option(
                "-y",
                "--yy",
                metavar = "Y",
                help = "a shorter but still long option"
            )
        )
        c.registerOption(
            c.option(
                "-z",
                "--zzzzzzzzzzzzz",
                metavar = "ZZZZZZZZ",
                help = "a short option"
            )
        )
        c.registerOption(
            c.option(
                "-t", "--entirely-too-long-option", metavar = "WOWSOLONG",
                help = "this option has a long name and a long description"
            )
        )
        doTest(
            """
            |Usage: prog [<options>]
            |
            |Options:
            |  -x=<x>...     one very very very very very very long
            |                option
            |  -y, --yy=<y>  a shorter but still long option
            |  -z, --zzzzzzzzzzzzz=<zzzzzzzz>
            |                a short option
            |  -t, --entirely-too-long-option=<wowsolong>
            |                this option has a long name and a long
            |                description
            """,
            width = 54
        )
    }

    @[Test JsName("help_output_option_groups")]
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
            |  This is the help text for the option group named
            |  Grouped. This text should wrap onto exactly three
            |  lines.
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
            width = 54,
            command = C()
        )
    }
}
