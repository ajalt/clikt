package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.mpp.graphemeLengthMpp
import com.github.ajalt.mordant.markdown.Markdown
import com.github.ajalt.mordant.rendering.*
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.widgets.DefinitionListBuilder
import com.github.ajalt.mordant.widgets.Padding
import com.github.ajalt.mordant.widgets.definitionList
import com.github.ajalt.mordant.widgets.withPadding

@Suppress("MemberVisibilityCanBePrivate")
open class MordantHelpFormatter(
        protected val localization: Localization = defaultLocalization,
        protected val indent: String = "  ",
        protected val requiredOptionMarker: String? = null,
        protected val showDefaultValues: Boolean = false,
        protected val showRequiredTag: Boolean = false,
        protected val terminal: Terminal = Terminal(theme = DefaultTheme)
) : HelpFormatter {
    companion object {
        val DefaultTheme = Theme(Theme.Plain) { flags["markdown.code.block.border"] = false }
        val ColorTheme = Theme(DefaultTheme) {
            styles["clikt.title"] = brightYellow
            styles["clikt.parameter"] = brightBlue
            styles["clikt.meta"] = TextStyle(brightBlue, dim = true)
            styles["clikt.tag"] = TextStyle(dim = true)
            styles["clikt.usage.optional"] = TextStyle(dim = true)
            flags["clikt.headers"] = true
        }
    }

    @Suppress("UNUSED_PARAMETER")
    // Compatibility with the pre-mordant constructor
    constructor(
            localization: Localization = defaultLocalization,
            indent: String = "  ",
            width: Int? = null,
            maxWidth: Int = 78,
            maxColWidth: Int? = null,
            colSpacing: Int = 2,
            requiredOptionMarker: String? = null,
            showDefaultValues: Boolean = false,
            showRequiredTag: Boolean = false
    ): this(
            localization,
            indent,
            requiredOptionMarker,
            showDefaultValues,
            showRequiredTag,
            Terminal(width = width ?: maxWidth, theme = DefaultTheme)
    )

    override fun formatUsage(parameters: List<HelpFormatter.ParameterHelp>, programName: String): String {
        return terminal.render(buildWidget { addUsage(parameters, programName) })
    }

    override fun formatUsageError(
        error: UsageError,
        parameters: List<HelpFormatter.ParameterHelp>,
        programName: String,
    ): String {
        TODO()
    }

    override fun formatHelp(
            prolog: String,
            epilog: String,
            parameters: List<HelpFormatter.ParameterHelp>,
            programName: String
    ): String = terminal.render(buildWidget {
        addUsage(parameters, programName)
        addProlog(prolog)
        if (parameters.isNotEmpty()) appendln()
        appendln(definitionList {
            inline = false
            addOptions(parameters)
            addArguments(parameters)
            addCommands(parameters)
        })

        addEpilog(epilog)
    })

    protected open fun WidgetBuilder.addUsage(
            parameters: List<HelpFormatter.ParameterHelp>,
            programName: String
    ) {
        val optionalStyle = terminal.theme.style("clikt.usage.optional")
        val prog = "${renderSectionTitle(localization.usageTitle())} $programName"
        val usage = buildString {
            if (parameters.any { it is HelpFormatter.ParameterHelp.Option }) {
                append(optionalStyle(localization.optionsMetavar()))
            }

            parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().forEach {
                append(" ")
                val t = (if (it.required) it.name else "[${it.name}]") + if (it.repeatable) "..." else ""
                val s = if (it.required) TextStyle() else optionalStyle
                append(s(t))
            }

            if (parameters.any { it is HelpFormatter.ParameterHelp.Subcommand }) {
                append(" ").append(optionalStyle(localization.commandMetavar()))
            }
        }

        if (usage.isEmpty()) {
            appendln(prog)
        } else {
            appendln(definitionList {
                entry(prog, usage)
                inline = true
                descriptionSpacing = 1
            })
        }
    }

    protected open fun WidgetBuilder.addProlog(prolog: String) {
        if (prolog.isNotEmpty()) {
            val markdown = Markdown(prolog.trimIndent(), showHtml = true)
            appendln(markdown.withPadding(Padding.of(top = 1, left = 2), padEmptyLines = false))
        }
    }

    protected open fun DefinitionListBuilder.addOptions(parameters: List<HelpFormatter.ParameterHelp>) {
        val groupsByName = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Group>().associateBy { it.name }
        parameters.filterIsInstance<HelpFormatter.ParameterHelp.Option>()
                .groupBy { it.groupName }
                .toList()
                .sortedBy { it.first == null }
                .forEach { (title, params) ->
                    val t = title?.let { "$it:" } ?: localization.optionsTitle()
                    addOptionGroup(t, groupsByName[title]?.help, params)
                }
    }

    protected open fun DefinitionListBuilder.addOptionGroup(title: String, help: String?, parameters: List<HelpFormatter.ParameterHelp.Option>) {
        val options = parameters.map {
            val names = mutableListOf(joinNamesForOption(it.names))
            if (it.secondaryNames.isNotEmpty()) names += joinNamesForOption(it.secondaryNames)
            DefinitionRow(
                    col1 = names.joinToString(" / ", postfix = optionMetavar(it)),
                    col2 = renderHelpText(it.help, it.tags),
                    marker = when (HelpFormatter.Tags.REQUIRED) {
                        in it.tags -> requiredOptionMarker?.let { m -> terminal.theme.style("clikt.tag")(m) }
                        else -> null
                    }
            )
        }
        if (options.isEmpty()) return

        entry {
            term(renderSectionTitle(title))
            description(buildWidget {
                if (help != null) {
                    appendln(Markdown(help, showHtml = true).withPadding(Padding.of(top = 1, left = 2), padEmptyLines = false))
                    appendln()
                }
                appendDefinitionList(options)
            })
        }
    }

    protected open fun DefinitionListBuilder.addArguments(parameters: List<HelpFormatter.ParameterHelp>) {
        val arguments = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Argument>().map {
            DefinitionRow(renderArgumentName(it.name), renderHelpText(it.help, it.tags))
        }
        if (arguments.isEmpty() || arguments.all { it.col2.isEmpty() }) return
        entry {
            term(renderSectionTitle(localization.argumentsTitle()))
            description(buildWidget { appendDefinitionList(arguments) })
        }
    }

    protected open fun DefinitionListBuilder.addCommands(parameters: List<HelpFormatter.ParameterHelp>) {
        val commands = parameters.filterIsInstance<HelpFormatter.ParameterHelp.Subcommand>().map {
            DefinitionRow(renderSubcommandName(it.name), renderHelpText(it.help, it.tags))
        }
        if (commands.isEmpty()) return
        entry {
            term(renderSectionTitle(localization.commandsTitle()))
            description(buildWidget { appendDefinitionList(commands) })
        }
    }

    protected open fun WidgetBuilder.addEpilog(epilog: String) {
        if (epilog.isEmpty()) return
        appendln()
        appendln(Markdown(epilog.trimIndent(), showHtml = true))
    }

    protected open fun renderHelpText(help: String, tags: Map<String, String>): String {
        val renderedTags = tags.asSequence()
                .filter { (k, v) -> shouldShowTag(k, v) }
                .joinToString(" ") { (k, v) -> renderTag(k, v) }
        val h = help.trimIndent()
        return if (renderedTags.isEmpty()) h else "$h $renderedTags"

    }

    protected open fun shouldShowTag(tag: String, value: String): Boolean {
        return when (tag) {
            HelpFormatter.Tags.DEFAULT -> showDefaultValues && value.isNotBlank()
            HelpFormatter.Tags.REQUIRED -> showRequiredTag
            else -> true
        }
    }

    protected open fun joinNamesForOption(names: Set<String>): String {
        return names.sortedBy { it.startsWith("--") }.joinToString(", ") { renderOptionName(it) }
    }

    protected open fun renderTag(tag: String, value: String): String {
        val t = when (tag) {
            HelpFormatter.Tags.DEFAULT -> localization.helpTagDefault()
            HelpFormatter.Tags.REQUIRED -> localization.helpTagRequired()
            else -> tag
        }
        return terminal.theme.style("clikt.tag")(if (value.isBlank()) "($t)" else "($t: $value)")
    }

    protected open fun renderOptionName(name: String): String = terminal.theme.style("clikt.parameter")(name)
    protected open fun renderArgumentName(name: String): String = terminal.theme.style("clikt.parameter")(name)
    protected open fun renderSubcommandName(name: String): String = terminal.theme.style("clikt.parameter")(name)
    protected open fun renderSectionTitle(title: String) = terminal.theme.style("clikt.title")(title)
    protected open fun renderMetavar(metavar: String) = terminal.theme.style("clikt.meta")(metavar)

    protected open fun optionMetavar(option: HelpFormatter.ParameterHelp.Option): String {
        if (option.metavar == null) return ""
        val metavar = " " + renderMetavar(option.metavar)
        if (option.nvalues.first > 1) return "$metavar..."
        return metavar
    }

    protected fun WidgetBuilder.appendDefinitionList(rows: List<DefinitionRow>) {
        if (rows.isEmpty()) return

        appendln(definitionList {
            inline = true
            for ((col1, col2, marker) in rows) {
                val termPrefix = when {
                    marker.isNullOrEmpty() -> indent
                    else -> marker + indent.drop(marker.graphemeLength).ifEmpty { " " }
                }
                entry {
                    term(termPrefix + col1, whitespace = Whitespace.PRE_WRAP)
                    description(Markdown(col2.trimIndent(), showHtml = true))
                }
            }
        })
    }

    /** The number of visible characters in a string */
    protected val String.graphemeLength: Int get() = graphemeLengthMpp

    protected data class DefinitionRow(val col1: String, val col2: String, val marker: String? = null)
}
