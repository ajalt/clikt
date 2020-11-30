package com.github.ajalt.clikt.completion

@Suppress("unused")
class FishCompletionTest : CompletionTestBase("fish") {
    override fun `custom completions expected`(): String {
        return ""
    }

    override fun `subcommands with multi-word names expected`(): String {
        return ""
    }

    override fun `option secondary names expected`(): String {
        return ""
    }

    override fun `explicit completion candidates expected`(): String {
        return ""
    }
}
