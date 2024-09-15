package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.rendering.Whitespace
import com.github.ajalt.mordant.rendering.Widget
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.widgets.Text
import com.github.ajalt.mordant.widgets.definitionList
import com.github.ajalt.mordant.widgets.withPadding

/**
 * Clikt's default HelpFormatter which uses Mordant to render its output.
 *
 * To customize help text, you can create a subclass and set it as the `helpFormatter` on your
 * command's context.
 */
open class MordantHelpFormatter(
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
) : AbstractHelpFormatter<Widget>(
    context,
    requiredOptionMarker,
    showDefaultValues,
    showRequiredTag
) {
    protected val theme: Theme get() = context.terminal.theme

    override fun formatHelp(
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String,
    ): String {
        val widget = verticalLayout {
            spacing = 1
            cellsFrom(collectHelpParts(error, prolog, epilog, parameters, programName))
        }
        return context.terminal.render(widget)
    }

    override fun renderError(parameters: List<ParameterHelp>, error: UsageError): Widget {
        return Text(renderErrorString(parameters, error))
    }

    override fun renderUsage(
        parameters: List<ParameterHelp>,
        programName: String,
    ): Widget {
        val title = styleUsageTitle(localization.usageTitle())
        val prog = "$title $programName"
        val usageParts = renderUsageParametersString(parameters)

        return if (usageParts.isEmpty()) {
            Text(prog, whitespace = Whitespace.NORMAL)
        } else {
            definitionList {
                entry(prog, Text(usageParts, whitespace = Whitespace.NORMAL))
                inline = true
                descriptionSpacing = 1
            }
        }
    }

    override fun renderProlog(prolog: String): Widget {
        return renderWrappedText(prolog).withPadding(padEmptyLines = false) { left = 2 }
    }

    override fun renderEpilog(epilog: String): Widget {
        return renderWrappedText(epilog)
    }

    override fun renderParameters(
        parameters: List<ParameterHelp>,
    ): Widget = definitionList {
        for (section in collectParameterSections(parameters)) {
            entry(section.title, section.content)
        }
    }

    override fun renderOptionGroup(
        help: String?,
        parameters: List<ParameterHelp.Option>,
    ): Widget {
        val options = parameters.map(::renderOptionDefinition)
        if (help == null) return buildParameterList(options)
        val markdown = renderWrappedText(help).withPadding(padEmptyLines = false) {
            top = 1
            left = 2
            bottom = 1
        }
        return verticalLayout {
            cell(markdown)
            cell(buildParameterList(options))
        }
    }

    override fun normalizeParameter(name: String): String = "<${name.lowercase()}>"
    override fun styleRequiredMarker(name: String): String = theme.style("danger")(name)
    override fun styleHelpTag(name: String): String = theme.style("muted")(name)
    override fun styleOptionName(name: String): String = theme.style("info")(name)
    override fun styleArgumentName(name: String): String = theme.style("info")(name)
    override fun styleSubcommandName(name: String): String = theme.style("info")(name)
    override fun styleSectionTitle(title: String): String = theme.style("warning")(title)
    override fun styleUsageTitle(title: String): String = theme.style("warning")(title)
    override fun styleError(title: String): String = theme.style("danger")(title)
    override fun styleOptionalUsageParameter(parameter: String): String {
        return theme.style("muted")(parameter)
    }

    override fun styleMetavar(metavar: String): String {
        return (theme.style("warning") + theme.style("muted"))(metavar)
    }

    override fun renderDefinitionTerm(row: DefinitionRow): Widget {
        val termPrefix = when {
            row.marker.isNullOrEmpty() -> "  "
            else -> row.marker + "  ".drop(row.marker!!.length).ifEmpty { " " }
        }
        return Text(termPrefix + row.term, whitespace = Whitespace.PRE_WRAP)
    }

    override fun renderDefinitionDescription(row: DefinitionRow): Widget {
        return if (row.description.isBlank()) Text("")
        else renderWrappedText(row.description)
    }

    override fun buildParameterList(rows: List<DefinitionRow>): Widget {
        return definitionList {
            inline = true
            for (row in rows) {
                entry(renderDefinitionTerm(row), renderDefinitionDescription(row))
            }
        }
    }

    open fun renderWrappedText(text: String): Widget = Text(text)
}
