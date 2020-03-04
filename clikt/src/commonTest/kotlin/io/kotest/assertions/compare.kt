/*
Copyright 2016 sksamuel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package io.kotest.assertions

fun compare(a: Any?, b: Any?): Boolean {
    return when (a) {
        is Int -> when (b) {
            is Long -> a.toLong() == b
            is Double -> a.toDouble() == b
            else -> a == b
        }
        is Float -> when (b) {
            is Double -> a.toDouble() == b
            else -> a == b
        }
        is Double -> when (b) {
            is Float -> a == b.toDouble()
            else -> a == b
        }
        is Long -> when (b) {
            is Int -> a == b.toLong()
            else -> a == b
        }
        else -> makeComparable(a) == makeComparable(b)
    }
}

private fun makeComparable(any: Any?): Any? {
    return when (any) {
        is BooleanArray -> any.asList()
        is IntArray -> any.asList()
        is ShortArray -> any.asList()
        is FloatArray -> any.asList()
        is DoubleArray -> any.asList()
        is LongArray -> any.asList()
        is ByteArray -> any.asList()
        is CharArray -> any.asList()
        is Array<*> -> any.asList()
        else -> any
    }
}
