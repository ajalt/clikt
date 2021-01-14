package com.github.ajalt.clikt.parameters.groups

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.parameters.internal.NullableLateinit
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parsers.OptionParser
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class MutuallyExclusiveOptionTransformContext(val context: Context)
typealias MutuallyExclusiveOptionsTransform<OptT, OutT> = MutuallyExclusiveOptionTransformContext.(List<OptT>) -> OutT

class MutuallyExclusiveOptions<OptT : Any, OutT> internal constructor(
    internal val options: List<OptionDelegate<out OptT?>>,
    override val groupName: String?,
    override val groupHelp: String?,
    internal val transformAll: MutuallyExclusiveOptionsTransform<OptT, OutT>,
) : ParameterGroupDelegate<OutT> {
    init {
        require(options.size > 1) { "must provide at least two options to a mutually exclusive group" }
    }

    private var value: OutT by NullableLateinit("Cannot read from group delegate before parsing command line")

    override operator fun provideDelegate(thisRef: CliktCommand, prop: KProperty<*>): ReadOnlyProperty<CliktCommand, OutT> {
        thisRef.registerOptionGroup(this)

        for (option in options) {
            require(option.names.isNotEmpty()) { "must specify names for all options in a group" }
            option.parameterGroup = this
            option.groupName = groupName
            thisRef.registerOption(option)
        }

        return this
    }

    override fun getValue(thisRef: CliktCommand, property: KProperty<*>): OutT = value

    override fun finalize(context: Context, invocationsByOption: Map<Option, List<OptionParser.Invocation>>) {
        for ((option, invocations) in invocationsByOption) {
            check(option in options) { "Internal Clikt Error: finalizing unregistered option [${option.names}]" }
            option.finalize(context, invocations)
        }

        for (option in options) {
            if (option !in invocationsByOption) {
                option.finalize(context, emptyList())
            }
        }

        val values = options.mapNotNull { it.value }
        value = MutuallyExclusiveOptionTransformContext(context).transformAll(values)
    }

    override fun postValidate(context: Context) {
        for (option in options) {
            option.postValidate(context)
        }
    }

    fun <T> copy(transformAll: MutuallyExclusiveOptionsTransform<OptT, T>) = MutuallyExclusiveOptions(options, groupName, groupHelp, transformAll)
}

/**
 * Set the name and help for this option.
 *
 * Although you would normally pass the name and help strings as arguments to
 * [mutuallyExclusiveOptions], this function can be more convenient for long help strings.
 *
 * @param name The name of the group.
 * @param help A help message to display for this group.
 */
fun <OptT: Any, OutT> MutuallyExclusiveOptions<OptT, OutT>.help(name: String, help: String): MutuallyExclusiveOptions<OptT, OutT> {
    return MutuallyExclusiveOptions(options, name, help, transformAll)
}

/**
 * Declare a set of two or more mutually exclusive options.
 *
 * If none of the options are given on the command line, the value of this delegate will be null.
 * If one option is given, the value will be that option's value.
 * If more than one option is given, the value of the last one is used.
 *
 * All options in the group must have a name specified. All options must be nullable (they cannot
 * use [flag], [required] etc.). If you want flags, you should use [switch] instead.
 *
 * ### Example:
 *
 * ```
 * val fruits: Int? by mutuallyExclusiveOptions(
 *   option("--apples").int(),
 *   option("--oranges").int()
 * )
 * ```
 *
 * @param name If given, the options in this group will be grouped together under this value in the
 * help output
 * @param help If given, this text will be added in help output to the group. If [name] is null,
 *   this value is not used.
 *
 * @see com.github.ajalt.clikt.parameters.options.switch
 * @see com.github.ajalt.clikt.parameters.types.choice
 */
@Suppress("unused")
fun <T : Any> ParameterHolder.mutuallyExclusiveOptions(
    option1: OptionDelegate<out T?>,
    option2: OptionDelegate<out T?>,
    vararg options: OptionDelegate<out T?>,
    name: String? = null,
    help: String? = null,
): MutuallyExclusiveOptions<T, T?> {
    return MutuallyExclusiveOptions(listOf(option1, option2) + options, name, help) { it.lastOrNull() }
}

/**
 * If more than one of the group's options are given on the command line, throw a [MutuallyExclusiveGroupException]
 */
fun <T : Any> MutuallyExclusiveOptions<T, T?>.single(): MutuallyExclusiveOptions<T, T?> = copy {
    if (it.size > 1) {
        throw MutuallyExclusiveGroupException(options.map { o -> o.longestName()!! })
    }
    it.lastOrNull()
}

/**
 * Make a [mutuallyExclusiveOptions] group required. If none of the options in the group are given,
 * a [UsageError] is thrown.
 */
fun <T : Any> MutuallyExclusiveOptions<T, T?>.required(): MutuallyExclusiveOptions<T, T> {
    return copy { values ->
        transformAll(values) ?: run {
            val names = options.joinToString { it.longestName()!! }
            throw UsageError(context.localization.requiredMutexOption(names))
        }
    }
}

/**
 * If none of the options in a [mutuallyExclusiveOptions] group are given on the command line, us [value] for the group.
 */
fun <T : Any> MutuallyExclusiveOptions<T, T?>.default(value: T): MutuallyExclusiveOptions<T, T> {
    return copy { transformAll(it) ?: value }
}
