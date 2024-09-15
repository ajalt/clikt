package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.definitionList
import com.github.ajalt.mordant.widgets.withPadding

/**
 * A [HelpFormatter] that uses Mordant to render its output as GitHub Flavored Markdown.
 *
 * To customize help text, you can create a subclass and set it as the `helpFormatter` on your
 * command's context.
 */
open class MordantMarkdownHelpFormatter(
    /**
     * The current command's context.
     */
    context: Context,
    /**
     * The string to show before the names of required options, or null to not show a mark.
     */
    requiredOptionMarker: String? = null,
    /**
     * If true, the default values will be shown in the help text for parameters that have them.
     */
    showDefaultValues: Boolean = false,
    /**
     * If true, a tag indicating the parameter is required will be shown after the description of
     * required parameters.
     */
    showRequiredTag: Boolean = false,
) : MordantHelpFormatter(
    context,
    requiredOptionMarker,
    showDefaultValues,
    showRequiredTag
) {
    override fun renderWrappedText(text: String): Widget = Markdown(text, showHtml = true)
}
