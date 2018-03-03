package com.github.ajalt.clikt.v2

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A container for a value that is initialized after the container is created.
 *
 * Similar to an unsyncronized lazy delegate, but the value is set manually. If the value is not set before
 * being read, it will return null if T is nullable, or throw an IllegalStateException otherwise.
 */
// TODO move to internal package
internal class ExplicitLazy<T>(private val errorMessage: String) : ReadWriteProperty<Any, T> {
    private var _value: Any? = null
    var value: T
        set(value) {
            _value = value
        }
        get() {

            try {
                @Suppress("UNCHECKED_CAST")
                return _value as T
            } catch (e: ClassCastException) {
                throw IllegalStateException(errorMessage)
            }
        }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}
