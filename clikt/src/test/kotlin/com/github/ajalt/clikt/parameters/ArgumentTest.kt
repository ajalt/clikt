package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.*
import com.github.ajalt.clikt.parameters.options.counted
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ArgumentTest {
    @Test
    fun `one required argument`() {
        class C : NeverCalledCliktCommand() {
            val foo by argument()
        }

        assertThrows<MissingParameter> { C().parse(splitArgv("")) }
                .hasMessage("Missing argument \"FOO\".")
    }

    @Test
    fun `one optional argument`() = parameterized(
            row("", null),
            row("asd", "asd")
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().optional()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one default argument`() = parameterized(
            row("", "def"),
            row("asd", "asd")
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().default("def")
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `defaultLazy argument`() = parameterized(
            row("", "default", true),
            row("foo", "foo", false)) { (argv, expected, ec) ->
        var called = false

        class C : CliktCommand() {
            val x by argument().defaultLazy { called = true; "default" }
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
                assertThat(called).isEqualTo(ec)
            }
        }

        assertThat(called).isFalse
        C().parse(splitArgv(argv))
    }

    @Test
    fun `one argument nvalues=2`() {
        class C : CliktCommand() {
            val x by argument().pair()
            override fun run() {
                assertThat(x).isEqualTo("1" to "2")
            }
        }

        C().parse(splitArgv("1 2"))

        assertThrows<MissingParameter> { C().parse(splitArgv("")) }
                .hasMessage("Missing argument \"X\".")
    }

    @Test
    fun `one optional argument nvalues=2`() = parameterized(
            row("", null),
            row("foo bar", "foo" to "bar")
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().pair().optional()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `one optional argument nvalues=3`() = parameterized(
            row("", null),
            row("foo bar baz", Triple("foo", "bar", "baz"))
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().triple().optional()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `misused arguments with nvalues=2`() {
        class C : NoRunCliktCommand() {
            val x by argument().pair()
        }
        assertThrows<IncorrectArgumentValueCount> { C().parse(splitArgv("foo")) }
                .hasMessage("argument X takes 2 values")
        assertThrows<UsageError> { C().parse(splitArgv("foo bar baz")) }
                .hasMessage("Got unexpected extra argument (baz)")
        assertThrows<UsageError> { C().parse(splitArgv("foo bar baz qux")) }
                .hasMessage("Got unexpected extra arguments (baz qux)")
    }

    @Test
    fun `misused arguments with nvalues=3`() {
        class C : NoRunCliktCommand() {
            val x by argument().triple()
        }

        assertThrows<IncorrectArgumentValueCount> { C().parse(splitArgv("foo bar")) }
                .hasMessage("argument X takes 3 values")
        assertThrows<UsageError> { C().parse(splitArgv("foo bar baz qux")) }
                .hasMessage("Got unexpected extra argument (qux)")

    }

    @Test
    fun `one argument nvalues=-1`() = parameterized(
            row("", emptyList()),
            row("foo", listOf("foo")),
            row("foo bar", listOf("foo", "bar")),
            row("foo bar baz", listOf("foo", "bar", "baz"))
    ) { (argv, expected) ->
        class C : CliktCommand() {
            val x by argument().multiple()
            override fun run() {
                assertThat(x).called("x").isEqualTo(expected)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `two arguments nvalues=-1,1`() = parameterized(
            row("foo", emptyList(), "foo"),
            row("foo bar", listOf("foo"), "bar"),
            row("foo bar baz", listOf("foo", "bar"), "baz")
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val foo by argument().multiple()
            val bar by argument()
            override fun run() {
                assertThat(foo).called("foo").isEqualTo(ex)
                assertThat(bar).called("bar").isEqualTo(ey)
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
        assertThrows<MissingParameter> {
            C().parse(splitArgv(""))
        }.hasMessageContaining("BAR")
    }

    @Test
    fun `two arguments nvalues=1,-1`() = parameterized(
            row("", null, emptyList<String>()),
            row("foo", "foo", emptyList()),
            row("foo bar", "foo", listOf("bar")),
            row("foo bar baz", "foo", listOf("bar", "baz"))
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val foo by argument().optional()
            val bar by argument().multiple()
            override fun run() {
                assertThat(foo).called("foo").isEqualTo(ex)
                assertThat(bar).called("bar").isEqualTo(ey)
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

        assertThrows<MissingParameter> { C().parse(splitArgv("")) }
                .hasMessageContaining("Missing argument \"FOO\".")
    }

    @Test
    fun `value -- with argument`() = parameterized(
            row("--xx --xx -- --xx", "--xx", "--xx"),
            row("--xx --xx bar --", "--xx", "bar")
    ) { (argv, ex, ey) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx")
            val y by argument()
            override fun run() {
                assertThat(x).called("x").isEqualTo(ex)
                assertThat(y).called("y").isEqualTo(ey)
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

        assertThrows<MissingParameter> { C().parse(splitArgv("")) }
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

        assertThrows<PrintHelpMessage> { C().parse(splitArgv("--help")) }
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
            softly {
                assertThat(x).isEqualTo("1")
                assertThat(y).isEqualTo(3)
                assertThat(z).isEqualTo("2")
            }
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
            softly {
                assertThat(x).isEqualTo("1")
                assertThat(y).isEqualTo(1)
                assertThat(z).isEqualTo("-y")
            }
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

        assertThrows<BadParameterValue> { C().parse(splitArgv("uerr")) }
                .matches {
                    it is BadParameterValue
                            && it.argument != null
                            && it.argument!!.name == "X"
                }
        assertThrows<BadParameterValue> { C().parse(splitArgv("err")) }
                .matches {
                    it is BadParameterValue
                            && it.argument != null
                            && it.argument!!.name == "X"
                }
    }

    @Test
    fun `multiple args with nvalues=-1`() {
        class C : NeverCalledCliktCommand() {
            val foo by argument().multiple()
            val bar by argument().multiple()
        }
        assertThrows<IllegalArgumentException> { C() }
    }

    @Test
    fun `punctuation in arg prefix unix style`() = parameterized(
            row("/foo"),
            row("\\foo")
    ) { (argv) ->
        class C : CliktCommand() {
            val x by argument()
            override fun run() {
                assertThat(x).called("x").isEqualTo(argv)
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `punctuation in arg prefix unix style error`() {
        class C : NeverCalledCliktCommand() {
            val x by argument()
        }
        assertThrows<NoSuchOption> { C().parse(splitArgv("-foo")) }
    }

    @Test
    fun `punctuation in arg prefix windows style`() = parameterized(
            row("-foo"),
            row("--foo")
    ) { (argv) ->
        class C : CliktCommand() {
            init {
                context { helpOptionNames = setOf("/help") }
            }

            val x by argument()
            override fun run() {
                assertThat(x).called("x").isEqualTo(argv)
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
        assertThrows<NoSuchOption> { C().parse(splitArgv("/foo")) }
    }
}
