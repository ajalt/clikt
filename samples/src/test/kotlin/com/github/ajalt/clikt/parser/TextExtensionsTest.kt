package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.testing.softForEach
import org.junit.Test

class TextExtensionsTest {
    @Test
    fun `wrapText`() {
        softForEach(
                "abc".wrapText() to "abc",
                "abc\n".wrapText() to "abc",
                "abc\n".wrapText(width = 2) to "abc",
                "abc".wrapText(width = 2) to "abc",
                "a c".wrapText(width = 2) to "a\nc",
                "a b c".wrapText(width = 3) to "a b\nc",
                "a bc".wrapText(width = 3) to "a\nbc",
                "abc".wrapText(initialIndent = "1", subsequentIndent = "2") to "1abc",
                "a b c".wrapText(width = 4, initialIndent = "1", subsequentIndent = "2") to "1a b\n2c",
                "a b c".wrapText(width = 3, initialIndent = "1 ") to "1 a\nb c",
                "a\n\nb".wrapText(width = 3, preserveParagraph = true) to "a\n\nb",
                "a b c\n\nd e f".wrapText(width = 3, preserveParagraph = true) to "a b\nc\n\nd e\nf",
                "a b c\n\nd e f".wrapText(width = 4, initialIndent = "1",
                        subsequentIndent = "2", preserveParagraph = true) to "1a b\n2c\n\n2d e\n2f",
                "".wrapText() to "",
                addDescription = false) { (actual, expected) ->
            assertThat(actual).isEqualTo(expected)
        }
    }

    @Test
    fun `appendRepeat`() {
        softForEach(
                Triple("a", 0, ""),
                Triple("a", 1, "a"),
                Triple("a", 2, "aa"),
                Triple("a", 3, "aaa"),
                Triple("ab", 2, "abab"),
                addDescription = false) {
            assertThat(StringBuilder().appendRepeat(it.first, it.second).toString())
                    .isEqualTo(it.third)
        }
    }
}
