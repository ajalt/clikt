package com.github.ajalt.clikt.internal

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.Invocation

internal fun finalizeOptions(
    context: Context,
    options: List<Option>,
    invocationsByOption: Map<Option, List<Invocation>>
) {
    // Finalize invoked options
    for ((option, invocations) in invocationsByOption) {
        option.finalize(context, invocations)
    }

    // Finalize uninvoked options
    val retries = mutableListOf<Option>()
    for (o in options.filter { it !in invocationsByOption }) {
        try {
            o.finalize(context, emptyList())
        } catch (e: IllegalStateException) {
            retries += o
        }
    }

    // If an uninvoked option triggers an ISE, retry it once after other options have been finalized
    // so that lazy defaults can reference other options.
    for (o in retries) {
        o.finalize(context, emptyList())
    }
}
