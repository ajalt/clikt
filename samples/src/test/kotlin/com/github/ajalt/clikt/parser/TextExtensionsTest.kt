package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import org.junit.Test

class TextExtensionsTest {
    @Test
    fun `wrapText`() = parameterized(
            row("abc".wrapText(), "abc"),
            row("abc\n".wrapText(), "abc"),
            row("abc\n".wrapText(width = 2), "abc"),
            row("abc".wrapText(width = 2), "abc"),
            row("a c".wrapText(width = 2), "a\nc"),
            row("a b c".wrapText(width = 3), "a b\nc"),
            row("a bc".wrapText(width = 3), "a\nbc"),
            row("abc".wrapText(initialIndent = "1", subsequentIndent = "2"), "1abc"),
            row("a b c".wrapText(width = 4, initialIndent = "1", subsequentIndent = "2"), "1a b\n2c"),
            row("a b c".wrapText(width = 3, initialIndent = "1 "), "1 a\nb c"),
            row("a\n\nb".wrapText(width = 3, preserveParagraph = true), "a\n\nb"),
            row("a b c\n\nd e f".wrapText(width = 3, preserveParagraph = true), "a b\nc\n\nd e\nf"),
            row("a b c\n\nd e f".wrapText(width = 4, initialIndent = "1",
                    subsequentIndent = "2", preserveParagraph = true), "1a b\n2c\n\n2d e\n2f"),
            row("".wrapText(), ""),
            addDescription = false) { (actual, expected) ->
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `appendRepeat`() = parameterized(
            row("a", 0, ""),
            row("a", 1, "a"),
            row("a", 2, "aa"),
            row("a", 3, "aaa"),
            row("ab", 2, "abab"),
            addDescription = false) { (text, repeat, expected) ->
        assertThat(StringBuilder().appendRepeat(text, repeat).toString()).isEqualTo(expected)
    }
}
