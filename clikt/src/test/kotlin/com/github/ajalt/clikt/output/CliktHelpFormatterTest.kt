package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.shouldBe
import org.junit.Test

private fun <T> l(vararg t: T) = listOf(*t)

private fun opt(
        names: List<String>,
        metavar: String? = null,
        help: String = "",
        nvalues: Int = 1,
        secondaryNames: List<String> = emptyList(),
        tags: Map<String, String> = emptyMap(),
        group: String? = null
): ParameterHelp.Option {
    return ParameterHelp.Option(names.toSet(), secondaryNames.toSet(), metavar, help, nvalues, tags, group)
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

class CliktHelpFormatterTest {
    @Test
    fun formatUsage() {
        val f = CliktHelpFormatter()
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
        val f = CliktHelpFormatter(width = 54)
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
        val f = CliktHelpFormatter(width = 54)
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
        val f = CliktHelpFormatter(width = 54)
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
        val f = CliktHelpFormatter(width = 60)
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
        val f = CliktHelpFormatter()
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
        val f = CliktHelpFormatter(width = 54)
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
    fun `formatHelp prolog preformat`() {
        val f = CliktHelpFormatter(width = 54)
        f.formatHelp(prolog = """Lorem ipsum dolor sit amet, consectetur adipiscing elit.

                ```
                - Morbi id libero purus.
                - Praesent sit amet neque tellus.
                ```
                ```
                - Vestibulum in condimentum turpis, in consectetur ex. Morbi id libero purus.
                ```

                Vivamus dictum varius massa, at euismod turpis maximus eu. Suspendisse molestie mauris at
                turpis bibendum egestas.
                """, epilog = "",
                parameters = l(),
                programName = "prog") shouldBe
                """
                |Usage: prog
                |
                |  Lorem ipsum dolor sit amet, consectetur adipiscing
                |  elit.
                |
                |  - Morbi id libero purus.
                |  - Praesent sit amet neque tellus.
                |
                |  - Vestibulum in condimentum turpis, in consectetur ex. Morbi id libero purus.
                |
                |  Vivamus dictum varius massa, at euismod turpis
                |  maximus eu. Suspendisse molestie mauris at turpis
                |  bibendum egestas.
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp parameter preformat`() {
        val f = CliktHelpFormatter(width = 54, maxColWidth = 12)
        f.formatHelp(prolog = "", epilog = "",
                parameters = l(
                        opt(l("-x"), nvalues = 0, help = "```Quisque viverra leo nec massa gravida congue.```"),
                        arg("FOO", help = """Lorem ipsum dolor sit amet, consectetur adipiscing elit.

                ```
                - Morbi id libero purus.
                - Praesent sit amet neque tellus.
                - Vestibulum in condimentum turpis, in consectetur ex. Morbi id libero purus.
                ```

                Vivamus dictum varius massa, at euismod turpis maximus eu. Suspendisse molestie mauris at
                turpis bibendum egestas.
                """),
                        arg("BAR", help = "Phasellus ultrices felis elit, ac interdum nibh dictum ac."),
                        arg("BAZ", help = """Mauris a sapien non est rhoncus accumsan.
                           ```
                           Nullam laoreet erat vel tempor viverra. Aliquam lacinia nisl ac varius dapibus.
                           ```
                        """),
                        sub("subcommand", """```
                            Morbi gravida, massa eu volutpat viverra, quam nunc tristique diam.```

                            Donec sed ligula blandit, luctus sem ac, sagittis risus.
                            """)
                ),
                programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS] [FOO] [BAR] [BAZ] COMMAND
                |            [ARGS]...
                |
                |Options:
                |  -x  Quisque viverra leo nec massa gravida congue.
                |
                |Arguments:
                |  FOO  Lorem ipsum dolor sit amet, consectetur
                |       adipiscing elit.
                |
                |       - Morbi id libero purus.
                |       - Praesent sit amet neque tellus.
                |       - Vestibulum in condimentum turpis, in consectetur ex. Morbi id libero purus.
                |
                |       Vivamus dictum varius massa, at euismod turpis
                |       maximus eu. Suspendisse molestie mauris at
                |       turpis bibendum egestas.
                |  BAR  Phasellus ultrices felis elit, ac interdum nibh
                |       dictum ac.
                |  BAZ  Mauris a sapien non est rhoncus accumsan.
                |
                |       Nullam laoreet erat vel tempor viverra. Aliquam lacinia nisl ac varius dapibus.
                |
                |Commands:
                |  subcommand  Morbi gravida, massa eu volutpat viverra, quam nunc tristique diam.
                |
                |              Donec sed ligula blandit, luctus sem ac,
                |              sagittis risus.
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp option wrapping`() {
        val f = CliktHelpFormatter(width = 54, maxColWidth = 12)
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
        val f = CliktHelpFormatter(width = 62)
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
    fun `formatHelp option groups`() {
        val f = CliktHelpFormatter(width = 54)
        f.formatHelp("", "", l(
                opt(l("--aa", "-a"), "INT", "some thing to live by aa", group = "Grouped"),
                opt(l("--bb", "-b"), "INT", "some thing to live by bb", group = "Singleton"),
                opt(l("--cc", "-c"), "INT", "some thing to live by cc", group = "Grouped"),
                opt(l("--dd", "-d"), "INT", "some thing to live by dd")
        ), programName = "prog") shouldBe
                """
                |Usage: prog [OPTIONS]
                |
                |Grouped:
                |  -a, --aa INT  some thing to live by aa
                |  -c, --cc INT  some thing to live by cc
                |
                |Singleton:
                |  -b, --bb INT  some thing to live by bb
                |
                |Options:
                |  -d, --dd INT  some thing to live by dd
                """.trimMargin("|")
    }

    @Test
    fun `formatHelp arguments`() {
        val f = CliktHelpFormatter(width = 54)
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
        val f = CliktHelpFormatter(width = 54)
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
    @Suppress("unused")
    fun `integration test with subcommand without help`() {
        class C : TestCommand() {
            val opt by option().flag()
        }

        class Sub : TestCommand()

        val c = C().subcommands(Sub())

        c.getFormattedHelp() shouldBe """
                |Usage: c [OPTIONS] COMMAND [ARGS]...
                |
                |Options:
                |  --opt
                |  -h, --help  Show this message and exit
                |
                |Commands:
                |  sub
                """.trimMargin("|")
    }

    @Test
    @Suppress("unused")
    fun `integration test`() {
        class G : OptionGroup("My Group", help = "this is my group") {
            val groupFoo by option(help = "foo for group").required()
            val bar by option("-g", "--group-bar", help = "bar for group")
        }

        class G2 : OptionGroup("Another group") {
            val groupBaz by option(help = "this group doesn't have help").required()
        }

        class C : NoRunCliktCommand(name = "program",
                help = """
                This is a program.

                This is the prolog.
                """,
                epilog = "This is the epilog") {
            val g by G().cooccurring()
            val g2 by G2().cooccurring()
            val ex by mutuallyExclusiveOptions(
                    option("--ex-foo", help = "exclusive foo"),
                    option("--ex-bar", help = "exclusive bar"),
                    name = "Exclusive",
                    help = "These options are exclusive"
            )
            val ex2 by mutuallyExclusiveOptions(
                    option("--ex-baz", help = "exclusive baz"),
                    option("--ex-qux", help = "exclusive qux"),
                    option("--ex-quz", help = "exclusive quz"),
                    name = "Exclusive without help"
            )
            val foo by option(help = "foo option help").int().required()
            val bar by option("-b", "--bar", help = "bar option help", metavar = "META").default("optdef")
            val baz by option(help = "baz option help").flag("--no-baz")
            val hidden by option(help = "hidden", hidden = true)
            val arg by argument()
            val multi by argument().multiple(required = true)

            init {
                context {
                    helpFormatter = CliktHelpFormatter(
                            showDefaultValues = true,
                            showRequiredTag = true,
                            requiredOptionMarker = "*"
                    )
                }
            }
        }

        class Sub : NoRunCliktCommand(help = """
            a subcommand

            with extra help
            """,
                helpTags = mapOf("deprecated" to ""))

        class Sub2 : NoRunCliktCommand(help = "another command")

        val c = C()
                .versionOption("1.0")
                .subcommands(Sub(), Sub2())

        c.getFormattedHelp() shouldBe """
                |Usage: program [OPTIONS] ARG [MULTI]... COMMAND [ARGS]...
                |
                |  This is a program.
                |
                |  This is the prolog.
                |
                |My Group:
                |
                |  this is my group
                |
                |* --group-foo TEXT      foo for group (required)
                |  -g, --group-bar TEXT  bar for group
                |
                |Another group:
                |* --group-baz TEXT  this group doesn't have help (required)
                |
                |Exclusive:
                |
                |  These options are exclusive
                |
                |  --ex-foo TEXT  exclusive foo
                |  --ex-bar TEXT  exclusive bar
                |
                |Exclusive without help:
                |  --ex-baz TEXT  exclusive baz
                |  --ex-qux TEXT  exclusive qux
                |  --ex-quz TEXT  exclusive quz
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
        val f = CliktHelpFormatter(width = 54, requiredOptionMarker = "*")
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
        val f = CliktHelpFormatter(width = 54, showRequiredTag = true)
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
        val f = CliktHelpFormatter(width = 54, showDefaultValues = true)
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
        val f = CliktHelpFormatter(width = 54)
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
        val f = CliktHelpFormatter(width = 54)
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
        val f = CliktHelpFormatter(width = 54)
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
