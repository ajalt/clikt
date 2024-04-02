package com.github.ajalt.clikt.internal

import com.github.ajalt.clikt.core.GroupableOption
import com.github.ajalt.clikt.core.MultiUsageError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.Option

internal val Option.group: ParameterGroup? get() = (this as? GroupableOption)?.parameterGroup

internal fun List<UsageError>.throwErrors() {
    MultiUsageError.buildOrNull(this)?.let { throw it }
}
