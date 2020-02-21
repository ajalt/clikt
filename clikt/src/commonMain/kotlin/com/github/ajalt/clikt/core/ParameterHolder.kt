package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.groups.ParameterGroup
import com.github.ajalt.clikt.parameters.options.Option

@DslMarker
annotation class ParameterHolderDsl

@ParameterHolderDsl
interface ParameterHolder {
    /**
     * Register an option with this command or group.
     *
     * This is called automatically for the built in options, but you need to call this if you want to add a
     * custom option.
     */
    fun registerOption(option: GroupableOption)
}

interface StaticallyGroupedOption : Option {
    /** The name of the group, or null if this option should not be grouped in the help output. */
    val groupName: String?
}

/**
 * An option that can be added to a [ParameterGroup]
 */
interface GroupableOption : StaticallyGroupedOption {
    /** The group that this option belongs to, or null. Set by the group. */
    var parameterGroup: ParameterGroup?

    /** The name of the group, or null if this option should not be grouped in the help output. */
    override var groupName: String?
}
