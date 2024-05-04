package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.mpp.graphemeLengthMpp
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.TextStyle
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
@Suppress("MemberVisibilityCanBePrivate")
open class MordantHelpFormatter(
    /**
     * The current command's context.
     */
    protected val context: Context,
    /**
     * The string to show before the names of required options, or null to not show a mark.
     */
    protected val requiredOptionMarker: String? = null,
    /**
     * If true, the default values will be shown in the help text for parameters that have them.
     */
    protected val showDefaultValues: Boolean = false,
    /**
     * If true, a tag indicating the parameter is required will be shown after the description of
     * required parameters.
     */
    protected val showRequiredTag: Boolean = false,
) : HelpFormatter {
    protected val localization: Localization get() = context.localization
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

    protected open fun collectHelpParts(
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String,
    ): List<Widget> = buildList {
        add(renderUsage(parameters, programName))
        if (error == null) {
            if (prolog.isNotEmpty()) add(renderProlog(prolog))
            if (parameters.isNotEmpty()) add(renderParameters(parameters))
            if (epilog.isNotEmpty()) add(renderEpilog(epilog))
        } else {
            add(renderError(parameters, error))
        }
    }

    protected open fun renderError(
        parameters: List<ParameterHelp>,
        error: UsageError,
    ): Widget {
        return Text(buildString {
            val errors = (error as? MultiUsageError)?.errors ?: listOf(error)
            for ((i, e) in errors.withIndex()) {
                if (i > 0) appendLine()
                append(styleError(localization.usageError()))
                append(" ")
                append(e.formatMessage(localization, parameterFormatter(context)))
            }
        })
    }

    protected open fun renderUsage(
        parameters: List<ParameterHelp>,
        programName: String,
    ): Widget {
        val optionalStyle = theme.style("muted")
        val title = styleUsageTitle(localization.usageTitle())
        val prog = "$title $programName"
        val usageParts = buildList {
            if (parameters.any { it is ParameterHelp.Option }) {
                val metavar = normalizeParameter(localization.optionsMetavar())
                add(optionalStyle(renderOptionalMetavar(metavar)))
            }

            parameters.filterIsInstance<ParameterHelp.Argument>().mapTo(this) {
                var name = normalizeParameter(it.name)
                if (!it.required) name = renderOptionalMetavar(name)
                if (it.repeatable) name = renderRepeatedMetavar(name)
                val style = if (it.required) TextStyle() else optionalStyle
                style(name)
            }

            if (parameters.any { it is ParameterHelp.Subcommand }) {
                val commandMetavar = normalizeParameter(localization.commandMetavar())
                val argsMetavar = normalizeParameter(localization.argumentsMetavar())
                val repeatedArgs = renderRepeatedMetavar(renderOptionalMetavar(argsMetavar))
                add(optionalStyle("$commandMetavar $repeatedArgs"))
            }
        }

        return if (usageParts.isEmpty()) {
            Text(prog, whitespace = Whitespace.NORMAL)
        } else {
            definitionList {
                entry(prog, Text(usageParts.joinToString(" "), whitespace = Whitespace.NORMAL))
                inline = true
                descriptionSpacing = 1
            }
        }
    }

    protected open fun renderProlog(prolog: String): Widget {
        return Markdown(prolog, showHtml = true).withPadding(padEmptyLines = false) { left = 2 }
    }

    protected open fun renderEpilog(epilog: String): Widget {
        return Markdown(epilog, showHtml = true)
    }

    protected open fun renderSectionTitle(title: String): String = "$title:"

    protected open fun renderOptions(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> {
        val groupsByName =
            parameters.filterIsInstance<ParameterHelp.Group>().associateBy { it.name }
        return parameters.filterIsInstance<ParameterHelp.Option>().groupBy { it.groupName }.toList()
            .sortedBy { it.first == null } // Put the ungrouped options last
            .filter { it.second.isNotEmpty() }.map { (title, params) ->
                val renderedTitle = renderSectionTitle(title ?: localization.optionsTitle())
                val content = renderOptionGroup(groupsByName[title]?.help, params)
                RenderedSection(styleSectionTitle(renderedTitle), content)
            }.toList()
    }


    protected open fun renderParameters(
        parameters: List<ParameterHelp>,
    ): Widget = definitionList {
        for (section in collectParameterSections(parameters)) {
            entry(section.title, section.content)
        }
    }

    protected open fun collectParameterSections(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> = buildList {
        addAll(renderOptions(parameters))
        addAll(renderArguments(parameters))
        addAll(renderCommands(parameters))
    }

    protected open fun renderOptionGroup(
        help: String?,
        parameters: List<ParameterHelp.Option>,
    ): Widget {
        val options = parameters.map {
            val unjoinedNames = if (it.acceptsNumberValueWithoutName) {
                listOf(numberOptionName(it)) + it.names
            } else {
                it.names
            }
            val names = mutableListOf(joinNamesForOption(unjoinedNames))
            if (it.secondaryNames.isNotEmpty()) names += joinNamesForOption(it.secondaryNames)
            DefinitionRow(
                term = names.joinToString(" / ", postfix = renderOptionValue(it)),
                description = renderParameterHelpText(it.help, it.tags),
                marker = when (HelpFormatter.Tags.REQUIRED) {
                    in it.tags -> requiredOptionMarker?.let { m ->
                        styleRequiredMarker(m)
                    }

                    else -> null
                })
        }
        if (help == null) return buildParameterList(options)
        val markdown = Markdown(help, showHtml = true).withPadding(padEmptyLines = false) {
            top = 1
            left = 2
            bottom = 1
        }
        return verticalLayout {
            cell(markdown)
            cell(buildParameterList(options))
        }
    }

    protected open fun renderArguments(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> {
        val arguments = parameters.filterIsInstance<ParameterHelp.Argument>().map {
            DefinitionRow(
                styleArgumentName(normalizeParameter(it.name)),
                renderParameterHelpText(it.help, it.tags)
            )
        }
        if (arguments.isEmpty() || arguments.all { it.description.isEmpty() }) return emptyList()
        val title = styleSectionTitle(renderSectionTitle(localization.argumentsTitle()))
        return listOf(RenderedSection(title, buildParameterList(arguments)))
    }

    protected open fun renderCommands(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> {
        val commands = parameters.filterIsInstance<ParameterHelp.Subcommand>().map {
            DefinitionRow(
                styleSubcommandName(it.name),
                renderParameterHelpText(it.help, it.tags)
            )
        }
        if (commands.isEmpty()) return emptyList()
        val title = styleSectionTitle(renderSectionTitle(localization.commandsTitle()))
        return listOf(RenderedSection(title, buildParameterList(commands)))
    }

    protected open fun renderParameterHelpText(
        help: String,
        tags: Map<String, String>,
    ): String {
        val renderedTags = tags.asSequence()
            .filter { (k, v) -> shouldShowTag(k, v) }
            .joinToString(" ") { (k, v) -> renderTag(k, v) }
        return when {
            renderedTags.isEmpty() -> help
            help.isEmpty() -> renderedTags
            else -> "$help $renderedTags"
        }
    }

    protected open fun shouldShowTag(tag: String, value: String): Boolean {
        return when (tag) {
            HelpFormatter.Tags.DEFAULT -> showDefaultValues && value.isNotBlank()
            HelpFormatter.Tags.REQUIRED -> showRequiredTag
            else -> true
        }
    }

    protected open fun joinNamesForOption(names: Iterable<String>): String {
        return names.sortedBy { it.startsWith("--") }
            .joinToString(", ") { styleOptionName(it) }
    }

    protected open fun renderTag(tag: String, value: String): String {
        val t = when (tag) {
            HelpFormatter.Tags.DEFAULT -> localization.helpTagDefault()
            HelpFormatter.Tags.REQUIRED -> localization.helpTagRequired()
            else -> tag
        }
        val fullTag = if (value.isBlank()) "($t)" else "($t: $value)"
        return when (tag) {
            HelpFormatter.Tags.REQUIRED -> styleRequiredMarker(fullTag)
            else -> styleHelpTag(fullTag)
        }
    }

    protected open fun numberOptionName(option: ParameterHelp.Option): String {
        val metavar = normalizeParameter(option.metavar ?: localization.intMetavar())
        return "${option.names.first().first()}$metavar"
    }

    protected open fun normalizeParameter(name: String): String = "<${name.lowercase()}>"
    protected open fun styleRequiredMarker(name: String): String = theme.style("danger")(name)
    protected open fun styleHelpTag(name: String): String = theme.style("muted")(name)
    protected open fun styleOptionName(name: String): String = theme.style("info")(name)
    protected open fun styleArgumentName(name: String): String = theme.style("info")(name)
    protected open fun styleSubcommandName(name: String): String = theme.style("info")(name)
    protected open fun styleSectionTitle(title: String): String = theme.style("warning")(title)
    protected open fun styleUsageTitle(title: String): String = theme.style("warning")(title)
    protected open fun styleError(title: String): String = theme.style("danger")(title)

    protected open fun styleMetavar(metavar: String): String {
        val style = theme.style("warning") + theme.style("muted")
        return style(metavar)
    }

    protected open fun parameterFormatter(context: Context): ParameterFormatter {
        return object : ParameterFormatter {
            override fun formatOption(name: String): String {
                return styleOptionName(name)
            }

            override fun formatArgument(name: String): String {
                return styleArgumentName(normalizeParameter(name))
            }

            override fun formatSubcommand(name: String): String {
                return styleSubcommandName(name)
            }
        }
    }

    protected open fun renderOptionalMetavar(metavar: String): String = "[$metavar]"
    protected open fun renderRepeatedMetavar(metavar: String): String = "$metavar..."
    protected open fun renderAttachedOptionValue(metavar: String) = "=$metavar"

    protected open fun renderOptionValue(option: ParameterHelp.Option): String {
        if (option.metavar == null) return ""
        var metavar = option.metavar.trim { it in "[]<>" }
        if ('|' !in metavar) metavar = normalizeParameter(metavar)
        metavar = styleMetavar(metavar)
        if (option.nvalues.last > 1) metavar = renderRepeatedMetavar(metavar)
        metavar = renderAttachedOptionValue(metavar)
        if (option.nvalues.first == 0) metavar = renderOptionalMetavar(metavar)
        return metavar
    }


    protected open fun renderDefinitionTerm(row: DefinitionRow): Widget {
        val termPrefix = when {
            row.marker.isNullOrEmpty() -> "  "
            else -> row.marker + "  ".drop(row.marker.graphemeLength).ifEmpty { " " }
        }
        return Text(termPrefix + row.term, whitespace = Whitespace.PRE_WRAP)
    }

    protected open fun renderDefinitionDescription(row: DefinitionRow): Widget {
        return if (row.description.isBlank()) Text("")
        else (Markdown(row.description, showHtml = true))
    }

    protected open fun buildParameterList(rows: List<DefinitionRow>): Widget {
        return definitionList {
            inline = true
            for (row in rows) {
                entry(renderDefinitionTerm(row), renderDefinitionDescription(row))
            }
        }
    }

    /** The number of visible characters in a string */
    protected val String.graphemeLength: Int get() = graphemeLengthMpp

    protected data class RenderedSection(val title: Widget, val content: Widget) {
        constructor(title: String, content: String) : this(Text(title), Text(content))
        constructor(title: String, content: Widget) : this(Text(title), content)
    }

    protected data class DefinitionRow(
        val term: String,
        val description: String,
        val marker: String? = null,
    )
}
