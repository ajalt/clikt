@file:Suppress("UnusedImport")

package com.github.ajalt.clikt.parameters.groups

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.core.MutuallyExclusiveGroupException
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import io.kotlintest.data.forall
import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Test

@Suppress("unused")
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

        class C : TestCommand() {
            val g by G()
            val o by option().default("d")

            override fun run_() {
                o shouldBe eo
                g.x shouldBe ex
                g.y shouldBe ey
            }
        }

        C().parse(argv)
    }

    @Test
    fun `plain option group with required option`() {
        class G : OptionGroup() {
            val x by option().required()
        }

        class C : TestCommand() {
            val g by G()
            override fun run_() {
                g.x shouldBe "foo"
            }
        }

        C().parse("--x=foo")

        shouldThrow<MissingParameter> {
            C().parse("")
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

        class C : TestCommand(called = false) {
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
            row("--x=1 --y=2", "2", "d"),
            row("--y=3", "3", "d"),
            row("--x=4 --o=5", "4", "5")
    ) { argv, eg, eo ->
        class C : TestCommand() {
            val o by option().default("d")
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"))

            override fun run_() {
                o shouldBe eo
                g shouldBe eg
            }
        }
        C().parse(argv)
    }

    @Test
    fun `mutually exclusive group single`() {
        class C(val runAllowed: Boolean) : TestCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"), option("--z")).single()
            override fun run_() {
                if (!runAllowed) fail("run should not be called")
            }
        }

        C(true).apply { parse("--x=1") }.g shouldBe "1"
        C(true).apply { parse("--y=1 --y=2") }.g shouldBe "2"

        shouldThrow<MutuallyExclusiveGroupException> { C(false).parse("--x=1 --y=2") }
                .message shouldBe "option --x cannot be used with --y or --z"

        shouldThrow<MutuallyExclusiveGroupException> { C(false).parse("--y=1 --z=2") }
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
        class C : TestCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y"))
            val h by mutuallyExclusiveOptions(option("--z"), option("--w"))
            override fun run_() {
                g shouldBe eg
                h shouldBe eh
            }
        }
        C().parse(argv)
    }

    @Test
    fun `mutually exclusive group duplicate option name`() {
        class C : TestCommand(called = false) {
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
        class C : TestCommand() {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y")).default("d")

            override fun run_() {
                g shouldBe eg
            }
        }
        C().parse(argv)
    }

    @Test
    fun `mutually exclusive group required`() {
        class C : TestCommand(called = false) {
            val g by mutuallyExclusiveOptions(option("--x"), option("--y")).required()
        }
        shouldThrow<UsageError> { C().parse("") }
                .message shouldBe "Must provide one of --x, --y"
    }

    @Test
    fun `co-occurring option group`() = forall(
            row("", false, null, null),
            row("--x=1", true, "1", null),
            row("--x=1 --y=2", true, "1", "2")
    ) { argv, eg, ex, ey ->
        class G : OptionGroup() {
            val x by option().required()
            val y by option()
        }

        class C : TestCommand() {
            val g by G().cooccurring()

            override fun run_() {
                if (eg) {
                    g?.x shouldBe ex
                    g?.y shouldBe ey
                } else {
                    g shouldBe null
                }
            }
        }

        C().parse(argv)
    }

    @Test
    fun `co-occurring option group enforcement`() {
        class GGG : OptionGroup() {
            val x by option().required()
            val y by option()
        }

        class C : TestCommand(called = false) {
            val g by GGG().cooccurring()
        }

        shouldThrow<UsageError> { C().parse("--y=2") }
                .message shouldBe "Missing option \"--x\"."
    }

    @Test
    fun `co-occurring option group with no required options`() {
        class GGG : OptionGroup() {
            val x by option()
            val y by option()
        }

        class C : TestCommand(called = false) {
            val g by GGG().cooccurring()
        }

        shouldThrow<IllegalArgumentException> { C() }
                .message shouldBe "At least one option in a co-occurring group must use `required()`"
    }

    @Test
    fun `choice group`() {
        class Group1 : OptionGroup() {
            val g11 by option().int().required()
            val g12 by option().int()
        }

        class Group2 : OptionGroup() {
            val g21 by option().int().required()
            val g22 by option().int()
        }

        class C : TestCommand() {
            val g by option().groupChoice("1" to Group1(), "2" to Group2())
        }
        forall(
                row("", 0, null, null),
                row("--g11=1 --g21=1", 0, null, null),
                row("--g=1 --g11=2", 1, 2, null),
                row("--g=1 --g11=2 --g12=3", 1, 2, 3),
                row("--g=1 --g11=2 --g12=3", 1, 2, 3),
                row("--g=2 --g21=2 --g22=3", 2, 2, 3),
                row("--g=2 --g11=2 --g12=3 --g21=2 --g22=3", 2, 2, 3)
        ) { argv, eg, eg1, eg2 ->
            with(C()) {
                parse(argv)
                when (eg) {
                    0 -> {
                        g shouldBe null
                    }
                    1 -> {
                        (g as Group1).g11 shouldBe eg1
                        (g as Group1).g12 shouldBe eg2
                    }
                    2 -> {
                        (g as Group2).g21 shouldBe eg1
                        (g as Group2).g22 shouldBe eg2
                    }
                }
            }
        }

        shouldThrow<BadParameterValue> { C().parse("--g=3") }
                .message shouldBe "Invalid value for \"--g\": invalid choice: 3. (choose from 1, 2)"
    }
}
