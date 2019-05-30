package com.github.ajalt.clikt.output

internal fun String.wrapText(
        sb: StringBuilder,
        width: Int = 78,
        initialIndent: String = "",
        subsequentIndent: String = ""
) {
    require(initialIndent.length < width) { "initialIndent >= width: ${initialIndent.length} >= $width" }
    require(subsequentIndent.length < width) { "subsequentIndent >= width: ${subsequentIndent.length} >= $width" }

    val split = split(Regex("""\n[ \t\r]*\n|(?<=```)\s+(?=```)"""))
    for ((i, paragraph) in split.withIndex()) {
        if (i > 0) sb.append("\n\n")
        sb.wrapParagraph(paragraph, width, if (i == 0) initialIndent else subsequentIndent, subsequentIndent)
    }
}

private fun StringBuilder.wrapParagraph(text: String, width: Int, initialIndent: String, subsequentIndent: String) {
    val pre = Regex("""\s*```((?:[^`]+|`{1,2}[^`])*)```\s*""")
            .matchEntire(text)?.groups?.get(1)?.value?.trim()
    if (pre != null) {
        for ((i, line) in pre.split(Regex("\r?\n")).withIndex()) {
            if (i == 0) append(initialIndent)
            else append("\n").append(subsequentIndent)
            append(line.trim())
        }
        return
    }

    if (initialIndent.length + text.length <= width) {
        append(initialIndent).append(text.trim())
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
