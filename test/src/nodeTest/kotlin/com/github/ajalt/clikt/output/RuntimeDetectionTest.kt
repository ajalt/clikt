package com.github.ajalt.clikt.output

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RuntimeDetectionTest {
    @Test
    fun detectingNodeRuntime() {
        defaultCliktConsole()::class.simpleName shouldBe "NodeCliktConsole"
    }
}
