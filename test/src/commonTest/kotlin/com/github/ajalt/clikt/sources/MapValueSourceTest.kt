package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.test.Test


class MapValueSourceTest {
    @Test
    fun getKey() = forAll(
            row("p_", null, false, "-", "p_foo-bar"),
            row("", ":", false, "-", "sub:foo-bar"),
            row("", ":", true, ":", "SUB:FOO:BAR"),
            row("", null, true, "-", "FOO-BAR"),
            row("", null, false, "_", "foo_bar")
    ) { p, j, c, r, k ->
        class Root : TestCommand()
        class Sub : TestCommand() {
            init {
                context {
                    valueSource = MapValueSource(mapOf(
                            "other" to "other",
                            "FX" to "fixed",
                            k to "foo"
                    ), getKey = ValueSource.getKey(p, j, c, r))
                }
            }

            val fooBar by option()
            val fixed by option(valueSourceKey = "FX")
            override fun run_() {
                fooBar shouldBe "foo"
                fixed shouldBe "fixed"
            }
        }
        Root().subcommands(Sub()).parse("sub")
    }


    @Test
    fun envvarKey() {
        class C : TestCommand() {
            init {
                context {
                    autoEnvvarPrefix = "A"
                    valueSource = MapValueSource(mapOf(
                            "FOO_E" to "foo",
                            "A_BAR" to "bar",
                            "B_V" to "baz"
                    ), getKey = ValueSource.envvarKey())
                }
            }

            val foo by option(envvar = "FOO_E")
            val bar by option()
            val baz by option(envvar = "B_E", valueSourceKey = "B_V")

            override fun run_() {
                foo shouldBe "foo"
                bar shouldBe "bar"
                baz shouldBe "baz"
            }
        }

        C().parse("")
    }
}
