package com.github.ajalt.clikt.internal

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.arguments.Argument
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.ArgumentInvocation
import com.github.ajalt.clikt.parsers.Invocation

internal fun finalizeOptions(
    context: Context,
    options: List<Option>,
    invocationsByOption: Map<Option, List<Invocation>>,
) {
    finalizeParameters(
        context, options, emptyList(), invocationsByOption, emptyList()
    )
}

private sealed class Param
private class Opt(val option: Option, val invs: List<Invocation>) : Param()
private class Arg(val argument: Argument, val invs: List<String>) : Param()
private class Group(val group: ParameterGroup, val invs: Map<Option, List<Invocation>>) : Param()

internal fun finalizeParameters(
    context: Context,
    options: List<Option>,
    groups: List<ParameterGroup>,
    optionInvocations: Map<Option, List<Invocation>>,
    argumentInvocations: List<ArgumentInvocation>,
) {
    val allGroups = optionInvocations.entries
        .groupBy({ it.key.group }, { it.key to it.value })
        .mapValuesTo(mutableMapOf()) { it.value.toMap() }

    // Add uninvoked params last so that e.g. we can skip prompting if there's an error in an
    // invoked option
    for (group in groups) {
        if (group !in allGroups) allGroups[group] = emptyMap()
    }

    val allOptions = (allGroups[null] ?: emptyMap()).toMutableMap()
    for (opt in options) {
        if (opt !in allOptions) allOptions[opt] = emptyList()
    }

    val allParams: List<Param> =
        argumentInvocations.map { Arg(it.argument, it.values) } +
                allOptions.map { Opt(it.key, it.value) } +
                allGroups.mapNotNull { it.key?.let { k -> Group(k, it.value) } }


    val errors = mutableListOf<UsageError>()
    var currentRound = allParams.toList()
    val nextRound = mutableListOf<Param>()

    while (true) {
        for (it in currentRound) {
            try {
                when (it) {
                    is Arg -> it.argument.finalize(context, it.invs)
                    is Opt -> it.option.finalize(context, it.invs)
                    is Group -> it.group.finalize(context, it.invs)
                }
            } catch (e: IllegalStateException) {
                nextRound += it
            } catch (e: UsageError) {
                errors += e
                context.errorEncountered = true
            } catch (e: Abort) {
                // ignore Abort if we already encountered an error
                if (!context.errorEncountered) throw e
            }
        }
        if (currentRound.size <= nextRound.size) break
        currentRound = nextRound.toList()
        nextRound.clear()
    }

    MultiUsageError.buildOrNull(errors)?.let { throw it }
}

private val Option.group: ParameterGroup? get() = (this as? GroupableOption)?.parameterGroup
