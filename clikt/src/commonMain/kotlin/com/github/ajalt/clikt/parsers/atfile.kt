package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.InvalidFileFormat
import com.github.ajalt.clikt.output.defaultLocalization

internal fun shlex(filename:String, text: String, context:Context?): List<String> {
    val localization = context?.localization ?: defaultLocalization
    val toks = mutableListOf<String>()
    var inQuote: Char? = null
    val sb = StringBuilder()
    var i = 0
    fun err(msg: String): Nothing {
        throw InvalidFileFormat(filename, msg, text.take(i).count { it == '\n' }, context)
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
                    sb.append(text[i + 1])
                    i += 2
                }
            }
            c == inQuote -> {
                toks += sb.toString()
                sb.clear()
                inQuote = null
                i += 1
            }
            c == '#' && inQuote == null -> {
                i = text.indexOf('\n', i)
                if (i < 0) break@loop
            }
            c in "\"'" && inQuote == null -> {
                inQuote = c
                i += 1
            }
            c.isWhitespace() && inQuote == null -> {
                if (sb.isNotEmpty()) {
                    toks += sb.toString()
                    sb.clear()
                }
                i += 1
            }
            else -> {
                sb.append(c)
                i += 1
            }
        }
    }

    if (inQuote != null) {
        err(localization.unclosedQuote())
    }

    if (sb.isNotEmpty()) {
        toks += sb.toString()
    }

    return toks
}
