package com.github.ajalt.clikt.output

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RuntimeDetectionTest {
    @Test
    fun detectingBrowserRuntime() {
        defaultCliktConsole()::class.simpleName shouldBe "BrowserCliktConsole"
    }
}
