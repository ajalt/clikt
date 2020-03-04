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

@Suppress("UNUSED_PARAMETER")
object Failures {
    fun failure(message: String): AssertionError = failure(message, null)
    fun failure(message: String, cause: Throwable?): AssertionError = AssertionError(message)
    fun clean(throwable: Throwable): Throwable = throwable
    fun failure(message: String, expectedRepr: String, actualRepr: String): Throwable = failure(message)
}
