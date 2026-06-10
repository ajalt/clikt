package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.NoSuchOption
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("unused")
class FlagOptionTest {
    @[Test JsName("negatable_derives_negation")]
    fun `negatable derives a --no- negation`() = forAll(
        row("", true),
        row("--cache", true),
        row("--no-cache", false),
        row("--no-cache --cache", true),
        row("--cache --no-cache", false),
    ) { argv, expected ->
        class C : TestCommand() {
            val cache by option("--cache").flag(default = true, negatable = true)
            override fun run_() {
                cache shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("negatable_derives_for_each_long_name")]
    fun `negatable derives a negation for each long name`() = forAll(
        row("--no-cache", false),
        row("--no-cc", false),
    ) { argv, expected ->
        class C : TestCommand() {
            val cache by option("-c", "--cache", "--cc").flag(default = true, negatable = true)
            override fun run_() {
                cache shouldBe expected
            }
        }
        C().parse(argv)
    }

    @[Test JsName("explicit_secondary_names_win_over_negatable")]
    fun `explicit secondary names win over negatable`() {
        class C : TestCommand() {
            val enabled by option("--enable").flag("--disable", default = true, negatable = true)
            override fun run_() {
                enabled shouldBe false
            }
        }
        C().parse("--disable")
        // negatable was overridden by the explicit --disable, so --no-enable was never registered
        shouldThrow<NoSuchOption> { C().parse("--no-enable") }
    }

    @[Test JsName("negatable_without_long_name_throws")]
    fun `negatable without a long name throws at construction`() {
        class C : TestCommand(called = false) {
            val quiet by option("-q").flag(negatable = true)
        }
        shouldThrow<IllegalArgumentException> { C() }
    }
}
