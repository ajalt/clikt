package com.github.ajalt.clikt.parameters.options

import kotlin.js.JsName
import kotlin.jvm.JvmName

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * call [fail][OptionTransformContext.fail] if the value is not valid.
 *
 * Your [validator] can also call [require][OptionTransformContext.require] to fail automatically if
 * an expression is false, or [message][OptionTransformContext.message] to show the user a warning
 * message without aborting.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.validate(
    validator: OptionValidator<AllT>,
): OptionDelegate<AllT> {
    return copy(transformValue, transformEach, transformAll, validator)
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * call [fail][OptionTransformContext.fail] if the value is not valid. The [validator] is not called
 * if the delegate value is null.
 *
 * Your [validator] can also call [require][OptionTransformContext.require] to fail automatically if
 * an expression is false, or [message][OptionTransformContext.message] to show the user a warning
 * message without aborting.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
 * ```
 */
@JvmName("nullableValidate")
@JsName("nullableValidate")
inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.validate(
    crossinline validator: OptionValidator<AllT>,
): OptionDelegate<AllT?> {
    return copy(transformValue, transformEach, transformAll, { if (it != null) validator(it) })
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [message] to include in the error
 * output.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check("value must be even") { it % 2 == 0 }
 * ```
 */
inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.check(
    message: String,
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] that returns a message
 * to include in the error output.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check(lazyMessage={"$it is not even"}) { it % 2 == 0 }
 * ```
 */
inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT, EachT, ValueT>.check(
    crossinline lazyMessage: (AllT) -> String = { it.toString() },
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}

/**
 * Check the final option value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [message] to include in the error
 * output. The [validator] is not called if the delegate value is null.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check("value must be even") { it % 2 == 0 }
 * ```
 */
@JvmName("nullableCheck")
@JsName("nullableCheck")
inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.check(
    message: String,
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT?> {
    return check({ message }, validator)
}

/**
 * Check the final argument value and raise an error if it's not valid.
 *
 * The [validator] is called with the final option type (the output of [transformAll]), and should
 * return `false` if the value is not valid. You can specify a [lazyMessage] that returns a message
 * to include in the error output. The [validator] is not called if the delegate value is null.
 *
 * You can use [validate] for more complex checks.
 *
 * ### Example:
 *
 * ```
 * val opt by option().int().check(lazyMessage={"$it is not even"}) { it % 2 == 0 }
 * ```
 */
@JvmName("nullableLazyCheck")
@JsName("nullableLazyCheck")
inline fun <AllT : Any, EachT, ValueT> OptionWithValues<AllT?, EachT, ValueT>.check(
    crossinline lazyMessage: (AllT) -> String = { it.toString() },
    crossinline validator: (AllT) -> Boolean,
): OptionDelegate<AllT?> {
    return validate { require(validator(it)) { lazyMessage(it) } }
}
