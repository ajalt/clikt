package com.github.ajalt.clikt.options

import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability

/**
 * A function that checks the type of a function parameter that will be passed a value from a Clickt parser.
 *
 * It should throw an exception if the given [KParameter] accepts does not accept arguments of the type
 * returned by the configured parameter parser.
 *
 * Due to type erasure, it's not possible to know the exact type of the parameter in all cases.
 * This function should not throw an exception unless the parameter type definitely does not
 * accept the values from this parser.
 *
 * @throws IllegalArgumentException if the type of [param] definitely does not accept the values
 *     from the configured parameter parser.
 */
typealias ParameterTargetChecker = (KParameter) -> Unit

class ParameterTargetCheckerBuilder(val param: KParameter) {
    inline fun <reified T> requireType(lazyMessage: () -> Any) {
        require(param.type.isSubtypeOf(T::class.starProjectedType.withNullability(true)), lazyMessage)
    }
}
