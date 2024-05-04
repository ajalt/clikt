package com.github.ajalt.clikt.output

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.output.HelpFormatter.ParameterHelp

@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractHelpFormatter<PartT>(
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

    protected open fun collectHelpParts(
        error: UsageError?,
        prolog: String,
        epilog: String,
        parameters: List<ParameterHelp>,
        programName: String,
    ): List<PartT> = buildList {
        add(renderUsage(parameters, programName))
        if (error == null) {
            if (prolog.isNotEmpty()) add(renderProlog(prolog))
            if (parameters.isNotEmpty()) add(renderParameters(parameters))
            if (epilog.isNotEmpty()) add(renderEpilog(epilog))
        } else {
            add(renderError(parameters, error))
        }
    }

    protected abstract fun renderError(parameters: List<ParameterHelp>, error: UsageError): PartT

    protected open fun renderErrorString(
        parameters: List<ParameterHelp>,
        error: UsageError,
    ): String = buildString {
        val errors = (error as? MultiUsageError)?.errors ?: listOf(error)
        for ((i, e) in errors.withIndex()) {
            if (i > 0) appendLine()
            append(styleError(localization.usageError()))
            append(" ")
            append(e.formatMessage(localization, parameterFormatter(context)))
        }
    }

    protected abstract fun renderUsage(parameters: List<ParameterHelp>, programName: String): PartT

    protected open fun renderUsageParametersString(parameters: List<ParameterHelp>): String {
        return buildList {
            if (parameters.any { it is ParameterHelp.Option }) {
                val metavar = normalizeParameter(localization.optionsMetavar())
                add(styleOptionalUsageParameter(renderOptionalMetavar(metavar)))
            }

            parameters.filterIsInstance<ParameterHelp.Argument>().mapTo(this) {
                var name = normalizeParameter(it.name)
                if (!it.required) name = renderOptionalMetavar(name)
                if (it.repeatable) name = renderRepeatedMetavar(name)
                if (it.required) styleRequiredUsageParameter(name)
                else styleOptionalUsageParameter(name)
            }

            if (parameters.any { it is ParameterHelp.Subcommand }) {
                val commandMetavar = normalizeParameter(localization.commandMetavar())
                val argsMetavar = normalizeParameter(localization.argumentsMetavar())
                val repeatedArgs = renderRepeatedMetavar(renderOptionalMetavar(argsMetavar))
                add(styleOptionalUsageParameter("$commandMetavar $repeatedArgs"))
            }
        }.joinToString(" ")
    }

    protected abstract fun renderProlog(prolog: String): PartT
    protected abstract fun renderEpilog(epilog: String): PartT
    protected abstract fun renderParameters(parameters: List<ParameterHelp>): PartT

    protected open fun renderSectionTitle(title: String): String = "$title:"

    protected open fun renderOptions(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection<PartT>> {
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

    protected open fun collectParameterSections(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection<PartT>> = buildList {
        addAll(renderOptions(parameters))
        addAll(renderArguments(parameters))
        addAll(renderCommands(parameters))
    }

    protected abstract fun renderOptionGroup(
        help: String?,
        parameters: List<ParameterHelp.Option>,
    ): PartT

    protected open fun renderOptionDefinition(it: ParameterHelp.Option): DefinitionRow {
        val unjoinedNames = if (it.acceptsNumberValueWithoutName) {
            listOf(numberOptionName(it)) + it.names
        } else {
            it.names
        }
        val names = mutableListOf(joinNamesForOption(unjoinedNames))
        if (it.secondaryNames.isNotEmpty()) names += joinNamesForOption(it.secondaryNames)
        return DefinitionRow(
            term = names.joinToString(" / ", postfix = renderOptionValue(it)),
            description = renderParameterHelpText(it.help, it.tags),
            marker = when (HelpFormatter.Tags.REQUIRED) {
                in it.tags -> requiredOptionMarker?.let { m ->
                    styleRequiredMarker(m)
                }

                else -> null
            })
    }

    protected open fun renderArguments(
        parameters: List<ParameterHelp>,
    ): List<RenderedSection<PartT>> {
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
    ): List<RenderedSection<PartT>> {
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
    protected open fun styleRequiredMarker(name: String): String = name
    protected open fun styleHelpTag(name: String): String = name
    protected open fun styleOptionName(name: String): String = name
    protected open fun styleArgumentName(name: String): String = name
    protected open fun styleSubcommandName(name: String): String = name
    protected open fun styleSectionTitle(title: String): String = title
    protected open fun styleUsageTitle(title: String): String = title
    protected open fun styleError(title: String): String = title
    protected open fun styleMetavar(metavar: String): String = metavar
    protected open fun styleOptionalUsageParameter(parameter: String): String = parameter
    protected open fun styleRequiredUsageParameter(parameter: String): String = parameter

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

    protected abstract fun renderDefinitionTerm(row: DefinitionRow): PartT
    protected abstract fun renderDefinitionDescription(row: DefinitionRow): PartT
    protected abstract fun buildParameterList(rows: List<DefinitionRow>): PartT

    protected data class RenderedSection<PartT>(val title: String, val content: PartT)

    protected data class DefinitionRow(
        val term: String,
        val description: String,
        val marker: String? = null,
    )
}
