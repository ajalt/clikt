package com.github.ajalt.clikt.parser

fun String.wrapText(width: Int = 78, initialIndent: String = "", subsequentIndent: String = "",
                    preserveParagraph: Boolean = false): String = buildString {
    wrapText(this, width, initialIndent, subsequentIndent, preserveParagraph)
}

fun String.wrapText(sb: StringBuilder, width: Int = 78, initialIndent: String = "",
                    subsequentIndent: String = "", preserveParagraph: Boolean = false) {
    require(initialIndent.length < width) { "initialIndent >= width: ${initialIndent.length} >= $width" }
    require(subsequentIndent.length < width) { "subsequentIndent >= width: ${subsequentIndent.length} >= $width" }
    with(sb) {
        if (preserveParagraph) {
            for ((i, paragraph) in this@wrapText.split("\n\n").withIndex()) {
                if (i > 0) append("\n\n")
                wrapParagraph(paragraph, width, if (i == 0) initialIndent else subsequentIndent, subsequentIndent)
            }
        } else {
            wrapParagraph(this@wrapText, width, initialIndent, subsequentIndent)
        }
    }
}

private fun StringBuilder.wrapParagraph(text: String, width: Int, initialIndent: String,
                                        subsequentIndent: String) {
    if (initialIndent.length + text.length <= width) {
        append(initialIndent).append(text.trim())
        return
    }
    val words = text.split(Regex("\\s+"))
    append(initialIndent)
    var currentWidth = initialIndent.length
    for ((i, word) in words.withIndex()) {
        if (word.isEmpty()) continue
        if (i > 0) {
            if (currentWidth + word.length >= width) {
                append("\n").append(subsequentIndent)
                currentWidth = subsequentIndent.length
            } else {
                append(" ")
                currentWidth += 1
            }
        }
        append(word)
        currentWidth += word.length
    }
}

fun StringBuilder.appendRepeat(text: String, repeat: Int): StringBuilder {
    ensureCapacity(capacity() + text.length * repeat)
    for (i in 0 until repeat) append(text)
    return this
}
