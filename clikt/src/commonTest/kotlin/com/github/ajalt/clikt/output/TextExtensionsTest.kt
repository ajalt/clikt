package com.github.ajalt.clikt.output

import io.kotest.data.forall
import io.kotest.matchers.shouldBe
import io.kotest.tables.row
import kotlin.js.JsName
import kotlin.test.Test

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
    fun splitParagraphs() = forall(
            row("a\nb", listOf("a\nb")),
            row("a\n\nb", listOf("a", "b")),
            row(" a \n \n b ", listOf("a", "b")),
            row("a\n\n```b```", listOf("a", "```b```")),
            row("```a```\n\n b ", listOf("```a```", "b")),
            row("a\n\n```\nb\n```", listOf("a", "```\nb\n```")),
            row("a\n```\nb\n```", listOf("a", "```\nb\n```")),
            row(" a \n ``` \n b \n ``` ", listOf("a", "``` \n b \n ```")),
            row("a \n \n \n ```\nb\n```\n```\nc\n```", listOf("a", "```\nb\n```", "```\nc\n```"))
    ) { text, ps ->
        splitParagraphs(text) shouldBe ps
    }

    @Test
    @JsName("wrapText_pre_minus_format_leading_indent")
    fun `wrapText pre-format leading indent`() {
        """
        a
        ```
        b    b
         c  c
          d
        ```
        e
        """.wrapText() shouldBe """
        |a
        |
        |b    b
        | c  c
        |  d
        |
        |e
        """.trimMargin()
    }
}
