package com.github.ajalt.clikt.output

internal fun String.wrapText(width: Int = 78, initialIndent: String = "", subsequentIndent: String = "",
                             preserveParagraph: Boolean = false): String = buildString {
    wrapText(this, width, initialIndent, subsequentIndent, preserveParagraph)
}

internal fun String.wrapText(sb: StringBuilder, width: Int = 78, initialIndent: String = "",
                             subsequentIndent: String = "", preserveParagraph: Boolean = false) {
    require(initialIndent.length < width) { "initialIndent >= width: ${initialIndent.length} >= $width" }
    require(subsequentIndent.length < width) { "subsequentIndent >= width: ${subsequentIndent.length} >= $width" }
    with(sb) {
        if (preserveParagraph) {
            for ((i, paragraph) in this@wrapText.split(Regex("\n[ \t\r]*\n")).withIndex()) {
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

    val withoutWrap = Regex("""^\s*#\{nowrap}[ \t]*\n""").replaceFirst(text, "")
    if (withoutWrap != text) {
        for ((i, line) in  withoutWrap.split("\n").withIndex()) {
            if (i == 0) append(initialIndent)
            else append("\n").append(subsequentIndent)
            append(line.trim())
        }
        return
    }

    val words = text.trim().split(Regex("\\s+"))
    append(initialIndent)
    var currentWidth = initialIndent.length
    for ((i, word) in words.withIndex()) {
        if (i > 0) {
            if (currentWidth + word.length + 1 > width) {
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

internal fun StringBuilder.appendRepeat(text: String, repeat: Int): StringBuilder {
    ensureCapacity(capacity() + text.length * repeat)
    for (i in 0 until repeat) append(text)
    return this
}
