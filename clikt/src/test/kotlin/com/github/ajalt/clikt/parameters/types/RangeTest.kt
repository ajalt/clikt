package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.pair
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import io.kotlintest.matchers.beEmpty
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test

class RangeTest {
    @Test
    fun `restrictTo option min`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(min = 1)
        }

        C().apply {
            parse(splitArgv(""))
            x shouldBe null
        }
        C().apply {
            parse(splitArgv("-x1"))
            x shouldBe 1
        }
        C().apply {
            parse(splitArgv("-x3"))
            x shouldBe 3
        }
        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx=0")) }
                .message shouldBe "Invalid value for \"--xx\": 0 is smaller than the minimum valid value of 1."
    }

    @Test
    fun `restrictTo option min clamp`() = parameterized(
            row("", null),
            row("--xx=1", 1),
            row("--xx -123", 1),
            row("-x0", 1)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(min = 1, clamp = true)
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `restrictTo option max`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(max = 1)
        }

        C().apply {
            parse(splitArgv(""))
            x shouldBe null
        }
        C().apply {
            parse(splitArgv("-x1"))
            x shouldBe 1
        }
        C().apply {
            parse(splitArgv("-x0"))
            x shouldBe 0
        }
        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx=2")) }
                .message shouldBe "Invalid value for \"--xx\": 2 is larger than the maximum valid value of 1."
    }

    @Test
    fun `restrictTo option max clamp`() = parameterized(
            row("", null),
            row("--xx=1", 1),
            row("--xx 123", 1),
            row("-x2", 1)) { (argv, expected) ->
        class C : CliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(max = 1, clamp = true)
            override fun run() {
                x shouldBe expected
            }
        }

        C().parse(splitArgv(argv))
    }

    @Test
    fun `restrictTo option range`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(1..2)
        }

        C().apply {
            parse(splitArgv(""))
            x shouldBe null
        }
        C().apply {
            parse(splitArgv("-x1"))
            x shouldBe 1
        }
        C().apply {
            parse(splitArgv("-x2"))
            x shouldBe 2
        }
        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx=3")) }
                .message shouldBe "Invalid value for \"--xx\": 3 is not in the valid range of 1 to 2."
        shouldThrow<BadParameterValue> { C().parse(splitArgv("-x0")) }
                .message shouldBe "Invalid value for \"-x\": 0 is not in the valid range of 1 to 2."
    }

    @Test
    fun `restrictTo option default`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(1..2).default(2)
            val y by option("-y", "--yy").int().restrictTo(min = 3, max = 4).default(3)
        }

        C().apply {
            parse(splitArgv(""))
            x shouldBe 2
            y shouldBe 3
        }
        C().apply {
            parse(splitArgv("-x1"))
            x shouldBe 1
            y shouldBe 3
        }
        C().apply {
            parse(splitArgv("-y4"))
            x shouldBe 2
            y shouldBe 4
        }
        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx=3")) }
                .message shouldBe "Invalid value for \"--xx\": 3 is not in the valid range of 1 to 2."
        shouldThrow<BadParameterValue> { C().parse(splitArgv("-y10")) }
                .message shouldBe "Invalid value for \"-y\": 10 is not in the valid range of 3 to 4."
    }

    @Test
    fun `restrictTo option multiple`() {
        class C : NoRunCliktCommand() {
            val x by option("-x", "--xx").int().restrictTo(1..2).multiple()
            val y by option("-y", "--yy").int().restrictTo(min = 3, max = 4).pair()
        }

        C().apply {
            parse(splitArgv(""))
            x should beEmpty()
            y shouldBe null
        }
        C().apply {
            parse(splitArgv("-x1 -x2"))
            x shouldBe listOf(1, 2)
            y shouldBe null
        }
        C().apply {
            parse(splitArgv("-y 3 4"))
            x should beEmpty()
            y shouldBe (3 to 4)
        }
        shouldThrow<BadParameterValue> { C().parse(splitArgv("--xx=3")) }
                .message shouldBe "Invalid value for \"--xx\": 3 is not in the valid range of 1 to 2."
        shouldThrow<BadParameterValue> { C().parse(splitArgv("-y10 1")) }
                .message shouldBe "Invalid value for \"-y\": 10 is not in the valid range of 3 to 4."
    }

    @Test
    fun `restrictTo argument`() {
        class C : NoRunCliktCommand() {
            val x by argument().int().restrictTo(min = 1, max = 2)
            val y by argument().int().restrictTo(3..4)
            val z by argument().int().restrictTo(min = 5, max = 6).optional()
            val w by argument().int().restrictTo(7..8).optional()
        }

        C().apply {
            parse(splitArgv("1 3 5 7"))
            x shouldBe 1
            y shouldBe 3
            z shouldBe 5
            w shouldBe 7
        }
        C().apply {
            parse(splitArgv("1 3"))
            x shouldBe 1
            y shouldBe 3
            z shouldBe null
            w shouldBe null
        }
        C().apply {
            parse(splitArgv("2 4 6 8"))
            x shouldBe 2
            y shouldBe 4
            z shouldBe 6
            w shouldBe 8
        }
        shouldThrow<BadParameterValue> { C().parse(splitArgv("0 4 6 8")) }
                .message shouldBe "Invalid value for \"X\": 0 is not in the valid range of 1 to 2."
        shouldThrow<BadParameterValue> { C().parse(splitArgv("1 4 6 10")) }
                .message shouldBe "Invalid value for \"W\": 10 is not in the valid range of 7 to 8."
    }

    @Test
    fun `restrictTo argument clamp`() {
        class C : NoRunCliktCommand() {
            val x by argument().int().restrictTo(min = 1, max = 2, clamp = true)
            val y by argument().int().restrictTo(3..4, clamp = true)
            val z by argument().int().restrictTo(min = 5, max = 6, clamp = true).optional()
            val w by argument().int().restrictTo(7..8, clamp = true).optional()
        }

        C().apply {
            parse(splitArgv("0 0 0 0"))
            x shouldBe 1
            y shouldBe 3
            z shouldBe 5
            w shouldBe 7
        }
        C().apply {
            parse(splitArgv("0 0"))
            x shouldBe 1
            y shouldBe 3
            z shouldBe null
            w shouldBe null
        }
        C().apply {
            parse(splitArgv("9 9 9 9"))
            x shouldBe 2
            y shouldBe 4
            z shouldBe 6
            w shouldBe 8
        }
    }
}
