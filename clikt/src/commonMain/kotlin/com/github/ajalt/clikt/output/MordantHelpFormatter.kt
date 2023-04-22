package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.mpp.graphemeLengthMpp
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.table.verticalLayout
import com.github.ajalt.mordant.widgets.*

/**
 * Clikt's default HelpFormatter which uses Mordant to render its output.
 *
 * To customize help text, you can create a subclass and set it as the `helpFormatter` on your
 * command's context.
 *
 * @param requiredOptionMarker The string to show before the names of required options, or null to
 *   not show a mark.
 * @param showDefaultValues If true, the default values will be shown in the help text for
 *   parameters that have them.
 * @param showRequiredTag If true, a tag indicating the parameter is required will be shown after
 * the description of required parameters.
 */
@Suppress("MemberVisibilityCanBePrivate")
open class MordantHelpFormatter(
    protected val requiredOptionMarker: String? = null,
    protected val showDefaultValues: Boolean = false,
    protected val showRequiredTag: Boolean = false,
) : HelpFormatter {
    override fun formatHelp(
        context: Context,
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String,
    ): String {
        val widget = verticalLayout {
            spacing = 1
            cellsFrom(collectHelpParts(context, error, prolog, epilog, parameters, programName))
        }
        return context.terminal.render(widget)
    }

    protected open fun collectHelpParts(
        context: Context,
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String,
    ): List<Widget> = buildList {
        add(renderUsage(context, parameters, programName))
        if (error == null) {
            if (prolog.isNotEmpty()) add(renderProlog(context, prolog))
            if (parameters.isNotEmpty()) add(renderParameters(context, parameters))
            if (epilog.isNotEmpty()) add(renderEpilog(context, epilog))
        } else {
            add(renderError(context, parameters, error))
        }
    }

    protected open fun renderError(
        context: Context,
        parameters: List<ParameterHelp>,
        error: UsageError,
    ): Widget {
        return Text(buildString {
            val errors = (error as? MultiUsageError)?.errors ?: listOf(error)
            for ((i, e) in errors.withIndex()) {
                if (i > 0) appendLine()
                append(styleError(context, context.localization.usageError()))
                append(" ")
                append(e.formatMessage(context.localization, parameterFormatter(context)))
            }
        })
    }

    protected open fun renderUsage(
        context: Context,
        parameters: List<ParameterHelp>,
        programName: String,
    ): Widget {
        val optionalStyle = context.terminal.theme.style("muted")
        val title = styleUsageTitle(context, context.localization.usageTitle())
        val prog = "$title $programName"
        val usageParts = buildList {
            if (parameters.any { it is ParameterHelp.Option }) {
                add(optionalStyle(context.localization.optionsMetavar()))
            }

            parameters.filterIsInstance<ParameterHelp.Argument>().mapTo(this) {
                val t =
                    (if (it.required) it.name else "[${it.name}]") + if (it.repeatable) "..." else ""
                val style = if (it.required) TextStyle() else optionalStyle
                style(t)
            }

            if (parameters.any { it is ParameterHelp.Subcommand }) {
                add(optionalStyle(context.localization.commandMetavar()))
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

    protected open fun renderProlog(context: Context, prolog: String): Widget {
        return Markdown(prolog, showHtml = true).withPadding(padEmptyLines = false) { left = 2 }
    }

    protected open fun renderEpilog(context: Context, epilog: String): Widget {
        return Markdown(epilog, showHtml = true)
    }

    protected open fun renderOptions(
        context: Context,
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> {
        val groupsByName =
            parameters.filterIsInstance<ParameterHelp.Group>().associateBy { it.name }
        return parameters.filterIsInstance<ParameterHelp.Option>().groupBy { it.groupName }.toList()
            .sortedBy { it.first == null } // Put the ungrouped options last
            .filter { it.second.isNotEmpty() }.map { (title, params) ->
                val renderedTitle = title?.let { "$it:" } ?: context.localization.optionsTitle()
                val content = renderOptionGroup(context, groupsByName[title]?.help, params)
                RenderedSection(styleSectionTitle(context, renderedTitle), content)
            }.toList()
    }

    protected open fun renderParameters(
        context: Context,
        parameters: List<ParameterHelp>,
    ): Widget = definitionList {
        for (section in collectParameterSections(context, parameters)) {
            entry(section.title, section.content)
        }
    }

    protected open fun collectParameterSections(
        context: Context,
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> = buildList {
        addAll(renderOptions(context, parameters))
        addAll(renderArguments(context, parameters))
        addAll(renderCommands(context, parameters))
    }

    protected open fun renderOptionGroup(
        context: Context,
        help: String?,
        parameters: List<ParameterHelp.Option>,
    ): Widget {
        val options = parameters.map {
            val unjoinedNames = if (it.acceptsNumberValueWithoutName) {
                listOf(numberOptionName(context, it)) + it.names
            } else {
                it.names
            }
            val names = mutableListOf(joinNamesForOption(context, unjoinedNames))
            if (it.secondaryNames.isNotEmpty()) names += joinNamesForOption(
                context, it.secondaryNames
            )
            DefinitionRow(col1 = names.joinToString(
                " / ",
                postfix = renderOptionValue(context, it)
            ),
                col2 = renderParameterHelpText(context, it.help, it.tags),
                marker = when (HelpFormatter.Tags.REQUIRED) {
                    in it.tags -> requiredOptionMarker?.let { m ->
                        styleRequiredMarker(context, m)
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
        context: Context,
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> {
        val arguments = parameters.filterIsInstance<ParameterHelp.Argument>().map {
            DefinitionRow(
                styleArgumentName(context, it.name),
                renderParameterHelpText(context, it.help, it.tags)
            )
        }
        if (arguments.isEmpty() || arguments.all { it.col2.isEmpty() }) return emptyList()
        val title = styleSectionTitle(context, context.localization.argumentsTitle())
        return listOf(RenderedSection(title, buildParameterList(arguments)))
    }

    protected open fun renderCommands(
        context: Context,
        parameters: List<ParameterHelp>,
    ): List<RenderedSection> {
        val commands = parameters.filterIsInstance<ParameterHelp.Subcommand>().map {
            DefinitionRow(
                styleSubcommandName(context, it.name),
                renderParameterHelpText(context, it.help, it.tags)
            )
        }
        if (commands.isEmpty()) return emptyList()
        val title = styleSectionTitle(context, context.localization.commandsTitle())
        return listOf(RenderedSection(title, buildParameterList(commands)))
    }

    protected open fun renderParameterHelpText(
        context: Context,
        help: String,
        tags: Map<String, String>,
    ): String {
        val renderedTags = tags.asSequence()
            .filter { (k, v) -> shouldShowTag(k, v) }
            .joinToString(" ") { (k, v) -> renderTag(context, k, v) }
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

    protected open fun joinNamesForOption(context: Context, names: Iterable<String>): String {
        return names.sortedBy { it.startsWith("--") }
            .joinToString(", ") { styleOptionName(context, it) }
    }

    protected open fun renderTag(context: Context, tag: String, value: String): String {
        val t = when (tag) {
            HelpFormatter.Tags.DEFAULT -> context.localization.helpTagDefault()
            HelpFormatter.Tags.REQUIRED -> context.localization.helpTagRequired()
            else -> tag
        }
        val fullTag = if (value.isBlank()) "($t)" else "($t: $value)"
        return when (tag) {
            HelpFormatter.Tags.REQUIRED -> styleRequiredMarker(context, fullTag)
            else -> styleHelpTag(context, fullTag)
        }
    }

    protected open fun numberOptionName(context: Context, option: ParameterHelp.Option): String {
        return "${
            option.names.first().first()
        }${option.metavar ?: context.localization.intMetavar()}"
    }

    protected open fun styleRequiredMarker(context: Context, name: String): String =
        context.terminal.theme.style("danger")(name)

    protected open fun styleHelpTag(context: Context, name: String): String =
        context.terminal.theme.style("muted")(name)

    protected open fun styleOptionName(context: Context, name: String): String =
        context.terminal.theme.style("info")(name)

    protected open fun styleArgumentName(context: Context, name: String): String =
        context.terminal.theme.style("info")(name)

    protected open fun styleSubcommandName(context: Context, name: String): String =
        context.terminal.theme.style("info")(name)

    protected open fun styleSectionTitle(context: Context, title: String): String =
        context.terminal.theme.style("warning")(title)

    protected open fun styleUsageTitle(context: Context, title: String): String =
        context.terminal.theme.style("warning")(title)

    protected open fun styleError(context: Context, title: String): String =
        context.terminal.theme.style("danger")(title)

    protected open fun styleMetavar(context: Context, metavar: String): String {
        val style = context.terminal.theme.style("warning") + context.terminal.theme.style("muted")
        return style(metavar)
    }

    protected open fun parameterFormatter(context: Context): ParameterFormatter {
        return { styleOptionName(context, it) }
    }

    protected open fun renderOptionValue(context: Context, option: ParameterHelp.Option): String {
        if (option.metavar == null) return ""
        var prefix = "="
        var suffix = if (option.nvalues.last > 1) "..." else ""
        if (option.nvalues.first == 0) {
            prefix = "[$prefix"
            suffix = "$suffix]"
        }
        val metavar = when (option.nvalues.first) {
            0 -> option.metavar.trim { it in "[]<>" }
            else -> option.metavar
        }
        return "$prefix${styleMetavar(context, metavar)}$suffix"
    }

    protected open fun buildParameterList(rows: List<DefinitionRow>): Widget {
        return definitionList {
            inline = true
            for ((col1, col2, marker) in rows) {
                val termPrefix = when {
                    marker.isNullOrEmpty() -> "  "
                    else -> marker + "  ".drop(marker.graphemeLength).ifEmpty { " " }
                }
                entry {
                    term(termPrefix + col1, whitespace = Whitespace.PRE_WRAP)
                    if (col2.isBlank()) description("")
                    else description(Markdown(col2, showHtml = true))
                }
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
        val col1: String,
        val col2: String,
        val marker: String? = null,
    )
}
