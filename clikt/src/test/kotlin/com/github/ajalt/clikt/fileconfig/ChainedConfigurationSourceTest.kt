package com.github.ajalt.clikt.fileconfig

import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.TestSource
import io.kotlintest.shouldBe
import org.junit.Test

class ChainedConfigurationSourceTest {

    @Test
    fun `reads from the first available value`() {
        val sources = listOf(
                TestSource(),
                TestSource("foo" to "bar"),
                TestSource("foo" to "baz")
        )

        class C : TestCommand() {
            init {
                context {
                    valueSources {
                        add(sources[0], sources[1])
                        +sources[2]
                    }
                }
            }

            val foo by option()

            override fun run_() {
                foo shouldBe "bar"
            }
        }

        with(C()) {
            parse("")
            sources[0].assert(true)
            sources[1].assert(true)
            sources[2].assert(false)
        }
    }
}

