package com.github.ajalt.clikt.internal

import com.github.ajalt.clikt.core.Abort
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.UsageError
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
    iterateFinalization(
        context, getAllOptions(invocationsByOption, options).map { Opt(it.key, it.value) }
    ).throwErrors()
}

private sealed class Param
private class Opt(val option: Option, val invs: List<Invocation>) : Param()
private class Arg(val argument: Argument, val invs: List<String>) : Param()
private class Group(val group: ParameterGroup, val invs: Map<Option, List<Invocation>>) : Param()

internal fun finalizeParameters(
    context: Context,
    options: List<Option>,
    groups: List<ParameterGroup>,
    arguments: List<Argument>,
    optionInvocations: Map<Option, List<Invocation>>,
    argumentInvocations: List<ArgumentInvocation>,
): List<UsageError> {
    // Add uninvoked params last so that e.g. we can skip prompting if there's an error in an
    // invoked option

    val allGroups = buildMap<ParameterGroup?, Map<Option, List<Invocation>>> {
        optionInvocations.entries
            .groupBy({ it.key.group }, { it.key to it.value })
            .mapValuesTo(this) { it.value.toMap() }
        for (group in groups) {
            if (group !in this) set(group, emptyMap())
        }
    }

    val allOptions = getAllOptions(allGroups[null] ?: emptyMap(), options)

    val allArguments = buildMap<Argument, List<String>> {
        argumentInvocations.associateTo(this) { it.argument to it.values }
        for (arg in arguments) {
            if (arg !in this) set(arg, emptyList())
        }
    }

    val allParams: List<Param> = buildList {
        allArguments.entries.mapTo(this) { Arg(it.key, it.value) }
        allOptions.entries.mapTo(this) { Opt(it.key, it.value) }
        allGroups.mapNotNullTo(this) { it.key?.let { k -> Group(k, it.value) } }
    }

    return iterateFinalization(context, allParams)
}

private fun getAllOptions(
    invocations: Map<Option, List<Invocation>>,
    options: List<Option>,
): Map<Option, List<Invocation>> {
    return buildMap {
        putAll(invocations)
        for (opt in options) {
            if (opt !in this) set(opt, emptyList())
        }
    }
}

private fun iterateFinalization(
    context: Context,
    allParams: List<Param>,
): MutableList<UsageError> {
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
                e.context = e.context ?: context
                errors += e
                context.errorEncountered = true
            } catch (e: Abort) {
                // ignore Abort if we already encountered an error
                if (!context.errorEncountered) throw e
            }
        }
        if (nextRound.isEmpty() || currentRound.size <= nextRound.size) break
        currentRound = nextRound.toList()
        nextRound.clear()
    }
    return errors
}

internal fun validateOptions(
    context: Context,
    optionInvocations: Map<Option, List<Invocation>>,
): List<UsageError> {
    val usageErrors = mutableListOf<UsageError>()
    optionInvocations.keys.filter { it.group == null }.forEach {
        gatherErrors(usageErrors, context) { it.postValidate(context) }
    }
    return usageErrors
}

internal fun validateParameters(
    context: Context,
    optionInvocations: Map<Option, List<Invocation>>,
): List<UsageError> {
    val usageErrors = mutableListOf<UsageError>()
    usageErrors += validateOptions(context, optionInvocations)
    context.command.registeredParameterGroups().forEach {
        gatherErrors(usageErrors, context) { it.postValidate(context) }
    }
    context.command.registeredArguments().forEach {
        gatherErrors(
            usageErrors,
            context
        ) { it.postValidate(context) }
    }
    return usageErrors
}

private inline fun gatherErrors(
    errors: MutableList<UsageError>,
    context: Context,
    block: () -> Unit,
) {
    try {
        block()
    } catch (e: UsageError) {
        e.context = e.context ?: context
        errors += e
        context.errorEncountered = true
    }
}
