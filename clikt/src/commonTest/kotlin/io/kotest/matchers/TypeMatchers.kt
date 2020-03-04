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

package io.kotest.matchers

import kotlin.reflect.KClass

// alias for beInstanceOf
fun instanceOf(expected: KClass<*>): Matcher<Any?> = beInstanceOf(expected)

fun beInstanceOf(expected: KClass<*>): Matcher<Any?> = neverNullMatcher { value ->
    MatcherResult(
            expected.isInstance(value),
            "$value is of type ${value::class.simpleName} but expected ${expected.simpleName}",
            "${value::class.simpleName} should not be of type ${expected.simpleName}"
    )
}

fun <T> beTheSameInstanceAs(ref: T): Matcher<T> = object : Matcher<T> {
    override fun test(value: T) = MatcherResult(value === ref, "$value should be the same reference as $ref", "$value should not be the same reference as $ref")
}

inline fun <U : Any, reified T : U> beInstanceOf2(): Matcher<U> = object : Matcher<U> {

    override fun test(value: U): MatcherResult =
            MatcherResult(
                    T::class.isInstance(value),
                    "$value is of type ${value::class.simpleName} but expected ${T::class.simpleName}",
                    "$value should not be an instance of ${T::class.simpleName}")

}


// checks that the given value is an instance (of type or of subtype) of T
inline fun <reified T : Any> beInstanceOf(): Matcher<Any?> = beInstanceOf(T::class)

fun beOfType(expected: KClass<*>): Matcher<Any?> = neverNullMatcher { value ->
    MatcherResult(
            expected == value::class,
            "$value should be of type ${expected.simpleName}",
            "$value should not be of type ${expected.simpleName}")
}

inline fun <reified T : Any> beOfType(): Matcher<Any?> = beOfType(T::class)
