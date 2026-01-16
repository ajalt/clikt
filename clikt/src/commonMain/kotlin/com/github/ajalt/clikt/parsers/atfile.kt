package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.defaultLocalization

internal fun shlex(
    filename: String,
    text: String,
    localization: Localization = defaultLocalization,
): List<String> {
    val toks = mutableListOf<String>()
    var inQuote: Char? = null
    var inToken = false // Track if we're building a token (for empty quoted strings)
    val sb = StringBuilder()
    var i = 0
    fun err(msg: String): Nothing {
        throw InvalidFileFormat(filename, msg, text.take(i).count { it == '\n' })
    }
    loop@ while (i < text.length) {
        val c = text[i]
        when {
            c in "\r\n" && inQuote != null -> {
                sb.append(c)
                i += 1
            }

            c == '\\' -> {
                if (i >= text.lastIndex) err(localization.fileEndsWithSlash())
                if (text[i + 1] in "\r\n") {
                    do {
                        i += 1
                    } while (i <= text.lastIndex && text[i].isWhitespace())
                } else {
                    inToken = true
                    sb.append(text[i + 1])
                    i += 2
                }
            }

            c == inQuote -> {
                // Don't emit here - just close the quote. Adjacent quoted/unquoted
                // strings should concatenate into a single token (POSIX behavior).
                inQuote = null
                i += 1
            }

            c == '#' && inQuote == null -> {
                i = text.indexOf('\n', i)
                if (i < 0) break@loop
            }

            c in "\"'" && inQuote == null -> {
                inToken = true
                inQuote = c
                i += 1
            }

            c.isWhitespace() && inQuote == null -> {
                if (inToken) {
                    toks += sb.toString()
                    sb.clear()
                    inToken = false
                }
                i += 1
            }

            else -> {
                inToken = true
                sb.append(c)
                i += 1
            }
        }
    }

    if (inQuote != null) {
        err(localization.unclosedQuote())
    }

    if (inToken) {
        toks += sb.toString()
    }

    return toks
}
