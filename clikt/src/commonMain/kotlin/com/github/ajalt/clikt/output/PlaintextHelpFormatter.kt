package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError

/**
 * A simple help formatter that outputs plain text.
 *
 * It doesn't support text wrapping, markdown, or any other styles or formatting.
 */
class PlaintextHelpFormatter(
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
) : AbstractHelpFormatter<String>(
    context,
    requiredOptionMarker,
    showDefaultValues,
    showRequiredTag
) {
    override fun formatHelp(
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String,
    ): String {
        val parts = collectHelpParts(error, prolog, epilog, parameters, programName)
        return parts.joinToString("\n\n")
    }

    override fun renderError(
        parameters: List<HelpFormatter.ParameterHelp>,
        error: UsageError,
    ): String = renderErrorString(parameters, error)

    override fun renderUsage(
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String,
    ): String {
        val params = renderUsageParametersString(parameters)
        val title = localization.usageTitle()
        return if (params.isEmpty()) "$title $programName"
        else "$title $programName $params"
    }

    override fun renderProlog(prolog: String): String {
        return prolog.lineSequence().joinToString("\n") { if (it.isEmpty()) "" else "  $it" }
    }

    override fun renderEpilog(epilog: String): String = epilog

    override fun renderParameters(parameters: List<HelpFormatter.ParameterHelp>): String {
        return collectParameterSections(parameters).joinToString("\n\n") { (title, content) ->
            "$title\n$content"
        }
    }

    override fun renderOptionGroup(
        help: String?,
        parameters: List<HelpFormatter.ParameterHelp.Option>,
    ): String = buildString {
        if (help != null) {
            appendLine(help)
            appendLine()
        }
        val options = parameters.map { renderOptionDefinition(it) }
        append(buildParameterList(options))
    }

    override fun renderDefinitionTerm(row: DefinitionRow): String {
        val termPrefix = when {
            row.marker.isNullOrEmpty() -> "  "
            else -> row.marker + "  ".drop(row.marker.length).ifEmpty { " " }
        }
        return termPrefix + row.term
    }

    override fun renderDefinitionDescription(row: DefinitionRow): String = row.description

    override fun buildParameterList(rows: List<DefinitionRow>): String {
        val termLength = (rows.maxOfOrNull { it.term.length } ?: 0) + 4
        return rows.joinToString("\n") {
            val term = renderDefinitionTerm(it)
            val definition = renderDefinitionDescription(it).ifBlank { null }
            val separator = " ".repeat(termLength - term.length)
            listOfNotNull(term, definition).joinToString(separator)
        }
    }
}
