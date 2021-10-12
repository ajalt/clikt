package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.MissingOption
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class OptionSwitchTest {
    @Test
    @JsName("switch_option_map")
    fun `switch option map`() = forAll(
        row("", null),
        row("-x", 1),
        row("-y", 2)
    ) { argv, ex ->
        class C : TestCommand() {
            val x by option().switch(mapOf("-x" to 1, "-y" to 2))
            override fun run_() {
                x shouldBe ex
            }
        }
        C().parse(argv)
    }

    @Test
    @JsName("switch_option_vararg")
    fun `switch option vararg`() = forAll(
        row("", null, -1, -2),
        row("-xyz", 1, 3, 5),
        row("--xx -yy -zz", 2, 4, 6),
    ) { argv, ex, ey, ez ->
        class C : TestCommand() {
            val x by option().switch("-x" to 1, "--xx" to 2)
            val y by option().switch("-y" to 3, "-yy" to 4).default(-1)
            val z by option().switch("-z" to 5, "-zz" to 6).defaultLazy { -2 }
            override fun run_() {
                x shouldBe ex
                y shouldBe ey
                z shouldBe ez
            }
        }

        C().parse(argv)
    }

    @Test
    @JsName("required_switch_options")
    fun `required switch options`() {
        class C : TestCommand() {
            val x by option().switch("-x" to 1, "-xx" to 2).required()
        }

        C().parse("-x").x shouldBe 1
        C().parse("-xx").x shouldBe 2
        shouldThrow<MissingOption> { C().parse("") }
    }
}
