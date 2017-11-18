package com.github.ajalt.clikt.options

import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

/** A class that converts option and argument values to a different type. */
interface ParamType<out T> {
    /** Given a value from the command line, return the value converted to this type. */
    fun convert(value: String): T

    /** Return a type that can be assigned values of [T] */
    val compatibleType: KType

    companion object {
        /** Create a new [ParamType] with the given conversion function. */
        inline fun <reified T> create(crossinline convert: (String) -> T)
                = object : ParamType<T> {
            override fun convert(value: String): T = convert(value)
            override val compatibleType: KType get() = T::class.starProjectedType
        }
    }
}

