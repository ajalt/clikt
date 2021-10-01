package com.github.ajalt.clikt.internal

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parsers.OptionParser

internal fun finalizeOptions(
    context: Context,
    options: List<Option>,
    invocationsByOption: Map<Option, List<OptionParser.Invocation>>,
) {
    // Finalize invoked options
    for ((option, invocations) in invocationsByOption) {
        option.finalize(context, invocations)
    }

    // Finalize uninvoked options
    for (o in options.filter { it !in invocationsByOption }) {
        o.finalize(context, emptyList())
    }
}
