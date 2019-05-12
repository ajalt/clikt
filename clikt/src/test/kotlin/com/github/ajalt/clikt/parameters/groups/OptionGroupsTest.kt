@file:Suppress("UnusedImport")

package com.github.ajalt.clikt.parameters.groups

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.testing.NeverCalledCliktCommand
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Test

class OptionGroupsTest {
    @Test
    fun `plain option group`() = forall(
            row("", null, "d", "d"),
            row("--x=1", "1", "d", "d"),
            row("--y=2", null, "2", "d"),
            row("--x=1 --y=2", "1", "2", "d"),
            row("--x=1 --y=2 --o=3", "1", "2", "3")
    ) { argv, ex, ey, eo ->
        class G : OptionGroup() {
            val x by option()
            val y by option().default("d")
        }

        class C : CliktCommand() {
            val g by G()
            val o by option().default("d")

            override fun run() {
                o shouldBe eo
                g.x shouldBe ex
                g.y shouldBe ey
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `plain option group with required option`() {
        class G : OptionGroup() {
            val x by option().required()
        }

        class C : CliktCommand() {
            val g by G()
            override fun run() {
                g.x shouldBe "foo"
            }
        }

        C().parse(splitArgv("--x=foo"))

        shouldThrow<MissingParameter> {
            C().parse(splitArgv(""))
        }.message shouldBe "Missing option \"--x\"."
    }

    @Test
    fun `plain option group duplicate option name`() {
        class G : OptionGroup() {
            val x by option()
        }

        class H : OptionGroup() {
            val x by option()
        }

        class C : NeverCalledCliktCommand() {
            val g by G()
            val h by H()
        }

        shouldThrow<IllegalArgumentException> { C() }
                .message shouldBe "Duplicate option name --x"
    }

    @Test
    fun `mutually exclusive group`() = forall(
            row("", null, "d"),
            row("--x=1", "1", "d"),
            row("--x=1 --x=2", "2", "d"),
            row("--y=3", "3", "d"),
            row("--x=4 --o=5", "4", "5")
    ) { argv, eg, eo ->
        class C : CliktCommand() {
            val o by option().default("d")
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"))

            override fun run() {
                o shouldBe eo
                g shouldBe eg
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `mutually exclusive group collision`() {
        class C : NeverCalledCliktCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"), option("--z"))
        }
        shouldThrow<MutuallyExclusiveGroupException> { C().parse(splitArgv("--x=1 --y=2")) }
                .message shouldBe "option --x cannot be used with --y or --z"

        shouldThrow<MutuallyExclusiveGroupException> { C().parse(splitArgv("--y=1 --z=2")) }
                .message shouldBe "option --x cannot be used with --y or --z"
    }

    @Test
    fun `multiple mutually exclusive groups`() = forall(
            row("", null, null),
            row("--x=1", "1", null),
            row("--y=2", "2", null),
            row("--z=3", null, "3"),
            row("--w=4", null, "4"),
            row("--x=5 --w=6", "5", "6")
    ) { argv, eg, eh ->
        class C : CliktCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"))
            val h by mutuallyExclusiveOptions(option("--z"), option("--w"))
            override fun run() {
                g shouldBe eg
                h shouldBe eh
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `mutually exclusive group duplicate option name`() {
        class C : NeverCalledCliktCommand() {
            val g by mutuallyExclusiveOptions(
                    option("--x"),
                    option("--x")
            )
        }

        shouldThrow<IllegalArgumentException> { C() }
                .message shouldBe "Duplicate option name --x"
    }

    @Test
    fun `mutually exclusive group default`() = forall(
            row("", "d"),
            row("--x=1", "1"),
            row("--x=2", "2")
    ) { argv, eg ->
        class C : CliktCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y")).default("d")

            override fun run() {
                g shouldBe eg
            }
        }
        C().parse(splitArgv(argv))
    }

    @Test
    fun `mutually exclusive group required`() {
        class C : NeverCalledCliktCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y")).required()
        }
        shouldThrow<UsageError> { C().parse(splitArgv("")) }
                .message shouldBe "Must provide one of --x, --y"
    }

    @Test
    fun `co-occuring option group`() = forall(
            row("", false, null, null),
            row("--x=1", true, "1", null),
            row("--x=1 --y=2", true, "1", "2")
    ) { argv, eg, ex, ey ->
        class G : OptionGroup() {
            val x by option().required()
            val y by option()
        }

        class C : CliktCommand() {
            val g by G().cooccurring()

            override fun run() {
                if (eg) {
                    g?.x shouldBe ex
                    g?.y shouldBe ey
                } else {
                    g shouldBe null
                }
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `co-occuring option group enforcement`() {
        class GGG : OptionGroup() {
            val x by option().required()
            val y by option()
        }

        class C : NeverCalledCliktCommand() {
            val g by GGG().cooccurring()
        }

        shouldThrow<UsageError> { C().parse(splitArgv("--y=2")) }
                .message shouldBe "Missing option \"--x\"."
    }

    @Test
    fun `co-occuring option group with no required options`() {
        class GGG : OptionGroup() {
            val x by option()
            val y by option()
        }

        class C : NeverCalledCliktCommand() {
            val g by GGG().cooccurring()
        }

        shouldThrow<IllegalArgumentException> { C() }
                .message shouldBe "At least one option in a co-occurring group must use `required()`"
    }
}
