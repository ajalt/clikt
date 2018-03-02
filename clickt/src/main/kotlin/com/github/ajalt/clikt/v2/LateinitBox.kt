package com.github.ajalt.clikt.v2

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private object UNINITIALIZED_VALUE

/**
 * A container for a value that is initialized after the container is created.
 *
 * Similar to an unsyncronized lazy delegate, but the value is set manually.
 */
internal class ExplicitLazy<T>(private val errorMessage: String) : ReadWriteProperty<Any, T> {
    private var _value: Any? = UNINITIALIZED_VALUE
    var value: T
        set(value) {
            _value = value
        }
        get() {
            require(_value != UNINITIALIZED_VALUE) { errorMessage }
            @Suppress("UNCHECKED_CAST")
            return _value as T
        }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}
