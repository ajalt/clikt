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

import io.kotest.assertions.Failures
import io.kotest.assertions.compare
import io.kotest.assertions.stringRepr

@Suppress("UNCHECKED_CAST")
infix fun <T, U : T> T.shouldBe(any: U?) {
    when (any) {
        is Matcher<*> -> should(any as Matcher<T>)
        else -> {
            if (this == null && any != null) {
                throw equalsError(any, this)
            } else if (!compare(this, any)) {
                throw equalsError(any, this)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
infix fun <T> T.shouldNotBe(any: Any?) {
    when (any) {
        is Matcher<*> -> shouldNot(any as Matcher<T>)
        else -> shouldNot(equalityMatcher(any))
    }
}

infix fun <T> T.shouldHave(matcher: Matcher<T>) = should(matcher)
infix fun <T> T.should(matcher: Matcher<T>) {
    val result = matcher.test(this)
    if (!result.passed()) {
        throw Failures.failure(result.failureMessage())
    }
}

infix fun <T> T.shouldNotHave(matcher: Matcher<T>) = shouldNot(matcher)
infix fun <T> T.shouldNot(matcher: Matcher<T>) = should(matcher.invert())

infix fun <T> T.should(matcher: (T) -> Unit) = matcher(this)

fun <T> be(expected: T) = equalityMatcher(expected)
fun <T> equalityMatcher(expected: T) = object : Matcher<T> {
    override fun test(value: T): MatcherResult {
        val expectedRepr = stringRepr(expected)
        val valueRepr = stringRepr(value)
        return MatcherResult(
                compare(expected, value),
                { equalsErrorMessage(expectedRepr, valueRepr) },
                { "$expectedRepr should not equal $valueRepr" }
        )
    }
}

private fun equalsError(expected: Any?, actual: Any?): Throwable {
    val (expectedRepr, actualRepr) = stringRepr(expected) to stringRepr(actual)
    val message = equalsErrorMessage(expectedRepr, actualRepr)
    return Failures.failure(message, expectedRepr, actualRepr)
}

private val linebreaks = Regex("\r?\n|\r")

// This is the format intellij requires to do the diff: https://github.com/JetBrains/intellij-community/blob/3f7e93e20b7e79ba389adf593b3b59e46a3e01d1/plugins/testng/src/com/theoryinpractice/testng/model/TestProxy.java#L50
private fun equalsErrorMessage(expected: Any?, actual: Any?): String {
    return when {
        expected is String && actual is String &&
                linebreaks.replace(expected, "\n") == linebreaks.replace(actual, "\n") -> {
            "line contents match, but line-break characters differ"
        }
        else -> "expected: $expected but was: $actual"
    }
}
