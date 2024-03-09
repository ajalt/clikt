package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlin.test.Test

class ContextJvmTest {
    @Test
    fun registerJvmCloseable() {
        var closed = 0
        class C: TestCommand() {
            override fun run_() {
                val c = AutoCloseable { closed += 1 }
                currentContext.registerJvmCloseable(c) shouldBeSameInstanceAs c
            }
        }
        C().parse("")
        closed shouldBe 1
    }
}
