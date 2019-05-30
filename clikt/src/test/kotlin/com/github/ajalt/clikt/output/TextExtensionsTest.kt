package com.github.ajalt.clikt.output

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.tables.row
import org.junit.Test

private fun String.wrapText(
        width: Int = 78,
        initialIndent: String = "",
        subsequentIndent: String = ""
): String = buildString {
    wrapText(this, width, initialIndent, subsequentIndent)
}

class TextExtensionsTest {
    @Test
    fun wrapText() = forall(
            row("abc".wrapText(), "abc"),
            row("abc\n".wrapText(), "abc"),
            row("abc\n".wrapText(width = 2), "abc"),
            row("abc".wrapText(width = 2), "abc"),
            row("a c".wrapText(width = 2), "a\nc"),
            row("a b c".wrapText(width = 3), "a b\nc"),
            row("a bc".wrapText(width = 3), "a\nbc"),
            row("abc".wrapText(initialIndent = "=", subsequentIndent = "-"), "=abc"),
            row("a b c".wrapText(width = 4, initialIndent = "=", subsequentIndent = "-"), "=a b\n-c"),
            row("a b c".wrapText(width = 3, initialIndent = "= "), "= a\nb c"),
            row("a\n\nb".wrapText(width = 3), "a\n\nb"),
            row("a b c\n\nd e f".wrapText(width = 3), "a b\nc\n\nd e\nf"),
            row("a b c\n\nd e f".wrapText(width = 4, initialIndent = "=",
                    subsequentIndent = "-"), "=a b\n-c\n\n-d e\n-f"),
            row("".wrapText(), "")
    ) { actual, expected ->
        actual shouldBe expected
    }

    @Test
    fun appendRepeat() = forall(
            row("a", 0, ""),
            row("a", 1, "a"),
            row("a", 2, "aa"),
            row("a", 3, "aaa"),
            row("ab", 2, "abab")
    ) { text, repeat, expected ->
        StringBuilder().appendRepeat(text, repeat).toString() shouldBe expected
    }
}
