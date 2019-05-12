package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.NeverCalledCliktCommand
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.data.forall
import io.kotlintest.matchers.string.contain
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.kotlintest.tables.row
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("unused")
class ArgumentTest {
    @Test
    fun `one required argument`() {
        class C : NeverCalledCliktCommand() {
            val foo by argument()
        }

        shouldThrow<MissingParameter> { C().parse(splitArgv("")) }
                .message shouldBe "Missing argument \"FOO\"."
    }

    @Test
    fun `one optional argument`() = forall(
            row("", null),
            row("-- --", "--")
    ) { argv, expected ->
        class C : CliktCommand() {
            val x by argument().optional()
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one default argument`() = forall(
            row("", "def")
    ) { argv, expected ->
        class C : CliktCommand() {
            val x by argument().default("def")
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `defaultLazy argument`() = forall(
            row("", "default", true)
    ) { argv, expected, ec ->
        var called = false

        class C : CliktCommand() {
            val x by argument().defaultLazy { called = true; "default" }
            override fun run() {
                x shouldBe expected
                called shouldBe ec
            }
        }

        called shouldBe false
        C().parse(splitArgv(argv))
    }

    @Test
    fun `one argument nvalues=2`() {
        class C : CliktCommand() {
            val x by argument().pair()
            override fun run() {
                x shouldBe ("1" to "2")
            }
        }

        C().parse(splitArgv("1 2"))

        shouldThrow<MissingParameter> { C().parse(splitArgv("")) }
                .message shouldBe "Missing argument \"X\"."
    }

    @Test
    fun `one optional argument nvalues=2`() = forall(
            row("", null)
    ) { argv, expected ->
        class C : CliktCommand() {
            val x by argument().pair().optional()
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one optional argument nvalues=3`() = forall(
            row("", null)
    ) { argv, expected ->
        class C : CliktCommand() {
            val x by argument().triple().optional()
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `misused arguments with nvalues=2`() {
        class C : NoRunCliktCommand() {
            val x by argument().pair()
        }
        shouldThrow<IncorrectArgumentValueCount> { C().parse(splitArgv("foo")) }
                .message shouldBe "argument X takes 2 values"
        shouldThrow<UsageError> { C().parse(splitArgv("foo bar baz")) }
                .message shouldBe "Got unexpected extra argument (baz)"
        shouldThrow<UsageError> { C().parse(splitArgv("foo bar baz qux")) }
                .message shouldBe "Got unexpected extra arguments (baz qux)"
    }

    @Test
    fun `misused arguments with nvalues=3`() {
        class C : NoRunCliktCommand() {
            val x by argument().triple()
        }

        shouldThrow<IncorrectArgumentValueCount> { C().parse(splitArgv("foo bar")) }
                .message shouldBe "argument X takes 3 values"
        shouldThrow<UsageError> { C().parse(splitArgv("foo bar baz qux")) }
                .message shouldBe "Got unexpected extra argument (qux)"

    }

    @Test
    fun `one argument multiple-unique nvalues=-1`() = forall(
            row("", emptySet()),
            row("foo foo", setOf("foo")),
            row("foo bar", setOf("foo", "bar"))
    ) { argv, expected ->
        val command = object : CliktCommand() {
            val x by argument().multiple().unique()
            override fun run() {
                x shouldBe expected
            }
        }
        command.parse(splitArgv(argv))
    }

    @Test
    fun `one argument nvalues=-1`() = forall(
            row("", emptyList()),
            row("foo", listOf("foo")),
            row("foo bar", listOf("foo", "bar")),
            row("foo bar baz", listOf("foo", "bar", "baz"))
    ) { argv, expected ->
        class C : CliktCommand() {
            val x by argument().multiple()
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one required argument nvalues=-1`() = forall(
            row("foo", listOf("foo")),
            row("foo bar", listOf("foo", "bar")),
            row("foo bar baz", listOf("foo", "bar", "baz"))
    ) { argv, expected ->
        class C : CliktCommand() {
            val x by argument().multiple(required = true)
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one required argument nvalues=-1, empty argv`() {
        class C : NoRunCliktCommand() {
            val x by argument().multiple(required = true)
        }

        shouldThrow<MissingParameter> { C().parse(splitArgv("")) }
    }

    @Test
    fun `two arguments nvalues=-1,1`() = forall(
            row("foo", emptyList(), "foo"),
            row("foo bar baz", listOf("foo", "bar"), "baz")
    ) { argv, ex, ey ->
        class C : CliktCommand() {
            val foo by argument().multiple()
            val bar by argument()
            override fun run() {
                foo shouldBe ex
                bar shouldBe ey
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two arguments nvalues=-1,1 empty argv`() {
        class C : NeverCalledCliktCommand() {
            val foo by argument().multiple()
            val bar by argument()
        }
        shouldThrow<MissingParameter> {
            C().parse(splitArgv(""))
        }.message!! should contain("BAR")
    }

    @Test
    fun `two arguments nvalues=1,-1`() = forall(
            row("", null, emptyList()),
            row("foo bar", "foo", listOf("bar")),
            row("foo bar baz", "foo", listOf("bar", "baz"))
    ) { argv, ex, ey ->
        class C : CliktCommand() {
            val foo by argument().optional()
            val bar by argument().multiple()
            override fun run() {
                foo shouldBe ex
                bar shouldBe ey
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two arguments nvalues=1,-1 empty argv`() {
        class C : NeverCalledCliktCommand() {
            val foo by argument()
            val bar by argument().multiple()
        }

        val ex = shouldThrow<MissingParameter> { C().parse(splitArgv("")) }
        ex.message!! should contain("Missing argument \"FOO\".")
    }

    @Test
    fun `value -- with argument`() = forall(
            row("--xx --xx -- --xx", "--xx", "--xx")
    ) { argv, ex, ey ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            val y by argument()
            override fun run() {
                x shouldBe ex
                y shouldBe ey
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `argument validator non-null`() {
        var called = false

        class C : NoRunCliktCommand() {
            val x: String by argument().validate {
                called = true
                require(it == "foo")
            }
        }

        C().parse(splitArgv("foo"))
        assertTrue(called)

        shouldThrow<MissingParameter> { C().parse(splitArgv("")) }
    }

    @Test
    fun `argument validator nullable`() {
        var called = false

        class C : NoRunCliktCommand() {
            val x: String? by argument().optional().validate {
                called = true
                require(it == "foo")
            }
        }

        C().parse(splitArgv("foo"))
        assertTrue(called)

        called = false
        C().parse(splitArgv(""))
        assertFalse(called)
    }

    @Test
    fun `eager option with required argument not given`() {
        class C : NeverCalledCliktCommand() {
            val x by argument()
        }

        shouldThrow<PrintHelpMessage> { C().parse(splitArgv("--help")) }
    }

    @Test
    fun `allowInterspersedArgs=true`() {
        class C : NoRunCliktCommand() {
            val x by argument()
            val y by option("-y").counted()
            val z by argument()
        }

        C().context { allowInterspersedArgs = true }.apply {
            parse(splitArgv("-y 1 -y 2 -y"))
            x shouldBe "1"
            y shouldBe 3
            z shouldBe "2"
        }
    }

    @Test
    fun `allowInterspersedArgs=false`() {
        class C : NoRunCliktCommand() {
            val x by argument()
            val y by option("-y").counted()
            val z by argument()
        }

        C().context { allowInterspersedArgs = false }.apply {
            parse(splitArgv("-y 1 -y"))
            x shouldBe "1"
            y shouldBe 1
            z shouldBe "-y"
        }
    }

    @Test
    fun `convert catches exceptions`() {
        class C : NoRunCliktCommand() {
            val x by argument().convert {
                when (it) {
                    "uerr" -> fail("failed")
                    "err" -> throw NumberFormatException("failed")
                }
                it
            }
        }

        var ex = shouldThrow<BadParameterValue> { C().parse(splitArgv("uerr")) }
        ex.argument shouldNotBe null
        ex.argument?.name shouldBe "X"

        ex = shouldThrow { C().parse(splitArgv("err")) }
        ex.argument shouldNotBe null
        ex.argument?.name shouldBe "X"
    }

    @Test
    fun `multiple args with nvalues=-1`() {
        class C : NeverCalledCliktCommand() {
            val foo by argument().multiple()
            val bar by argument().multiple()
        }
        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    fun `punctuation in arg prefix unix style`() = forall(
            row("/foo")
    ) { argv ->
        class C : CliktCommand() {
            val x by argument()
            override fun run() {
                x shouldBe argv
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `punctuation in arg prefix unix style error`() {
        class C : NeverCalledCliktCommand() {
            val x by argument()
        }
        shouldThrow<NoSuchOption> { C().parse(splitArgv("-foo")) }
    }

    @Test
    fun `punctuation in arg prefix windows style`() = forall(
            row("-foo"),
            row("--foo")
    ) { argv ->
        class C : CliktCommand() {
            init {
                context { helpOptionNames = setOf("/help") }
            }

            val x by argument()
            override fun run() {
                x shouldBe argv
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `punctuation in arg prefix windows style error`() {
        class C : NeverCalledCliktCommand() {
            init {
                context { helpOptionNames = setOf("/help") }
            }

            val x by argument()
        }
        shouldThrow<NoSuchOption> { C().parse(splitArgv("/foo")) }
    }
}
