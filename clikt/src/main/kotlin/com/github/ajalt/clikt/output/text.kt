package com.github.ajalt.clikt.output

internal fun String.wrapText(
        sb: StringBuilder,
        width: Int = 78,
        initialIndent: String = "",
        subsequentIndent: String = ""
) {
    require(initialIndent.length < width) { "initialIndent >= width: ${initialIndent.length} >= $width" }
    require(subsequentIndent.length < width) { "subsequentIndent >= width: ${subsequentIndent.length} >= $width" }

    for ((i, paragraph) in splitParagraphs(this).withIndex()) {
        if (i > 0) sb.append("\n\n")
        sb.wrapParagraph(paragraph, width, if (i == 0) initialIndent else subsequentIndent, subsequentIndent)
    }
}

private val TEXT_START_REGEX = Regex("\\S")
private val PRE_P_END_REGEX = Regex("""```[ \t]*(?:\n\s*|[ \t]*$)""")
private val PLAIN_P_END_REGEX = Regex("""[ \t]*\n(?:\s*```|[ \t]*\n\s*)|\s*$""")
private val PRE_P_CONTENTS_REGEX = Regex("""(?s)```\s*(.*?)\s*```""")

internal fun splitParagraphs(text: String): List<String> {
    val paragraphs = mutableListOf<String>()
    var i = TEXT_START_REGEX.find(text)?.range?.first ?: return emptyList()
    while (i < text.length) {
        val end = if (text.startsWith("```", startIndex = i)) {
            PRE_P_END_REGEX.find(text, startIndex = i + 3)?.let {
                (it.range.start + 3)..it.range.last
            }
        } else {
            PLAIN_P_END_REGEX.find(text, startIndex = i)?.let {
                if (it.value.endsWith("```")) it.range.start..(it.range.last - 3)
                else it.range
            }
        } ?: text.length..text.length
        paragraphs += text.substring(i, end.first)
        i = end.last + 1
    }
    return paragraphs
}

private fun StringBuilder.wrapParagraph(text: String, width: Int, initialIndent: String, subsequentIndent: String) {
    val pre = PRE_P_CONTENTS_REGEX.matchEntire(text)?.groups?.get(1)?.value
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
