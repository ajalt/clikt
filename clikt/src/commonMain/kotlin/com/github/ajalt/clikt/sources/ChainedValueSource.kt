package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option

/**
 * A [ValueSource] that looks for values in multiple other sources.
 */
class ChainedValueSource(val sources: List<ValueSource>) : ValueSource {
    init {
        require(sources.isNotEmpty()) { "Must provide configuration sources" }
    }

    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        return sources.asSequence()
                .map { it.getValues(context, option) }
                .firstOrNull { it.isNotEmpty() }
                .orEmpty()
    }
}
