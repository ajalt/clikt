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

/** Return a string representation of [obj] that is less ambiguous than `toString` */
fun stringRepr(obj: Any?): String = when (obj) {
    is Float -> "${obj}f"
    is Long -> "${obj}L"
    is Char -> "'$obj'"
    is String -> "\"$obj\""
    is Array<*> -> obj.map { recursiveRepr(obj, it) }.toString()
    is BooleanArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is IntArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is ShortArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is FloatArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is DoubleArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is LongArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is ByteArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is CharArray -> obj.map { recursiveRepr(obj, it) }.toString()
    is Iterable<*> -> obj.map { recursiveRepr(obj, it) }.toString()
    is Map<*, *> -> obj.map { (k, v) -> recursiveRepr(obj, k) to recursiveRepr(obj, v) }.toMap().toString()
    else -> obj.toString()
}

private fun recursiveRepr(root: Any, node: Any?): String {
    return if (root == node) "(this ${root::class.simpleName})" else stringRepr(node)
}
