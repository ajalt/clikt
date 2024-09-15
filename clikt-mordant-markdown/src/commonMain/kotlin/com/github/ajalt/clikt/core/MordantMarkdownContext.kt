package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.output.MordantMarkdownHelpFormatter

/**
 * Set up this command's context to use Mordant for rendering output as Markdown.
 */
fun BaseCliktCommand<*>.installMordantMarkdown() {
    installMordant(force = true)
    configureContext {
        helpFormatter = { MordantMarkdownHelpFormatter(it) }
    }
}

