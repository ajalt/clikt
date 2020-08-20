package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.TestSource
import com.github.ajalt.clikt.testing.parse
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class ChainedValueSourceTest {
    @Test
    @JsName("reads_from_the_first_available_value")
    fun `reads from the first available value`() {
        val sources = arrayOf(
                TestSource(),
                TestSource("foo" to "bar"),
                TestSource("foo" to "baz")
        )

        class C : TestCommand() {
            init {
                context {
                    valueSources(*sources)
                }
            }

            val foo by option()

            override fun run_() {
                foo shouldBe "bar"
            }
        }

        C().parse("")
        sources[0].assert(read = true)
        sources[1].assert(read = true)
        sources[2].assert(read = false)
    }
}
