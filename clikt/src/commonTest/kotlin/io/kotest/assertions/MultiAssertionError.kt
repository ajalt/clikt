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


/** An error that bundles multiple other [Throwable]s together */
class MultiAssertionError(errors: List<Throwable>) : AssertionError(createMessage(errors)) {
    companion object {
        private fun createMessage(errors: List<Throwable>) = buildString {
            append("\nThe following ")

            if (errors.size == 1) {
                append("assertion")
            } else {
                append(errors.size).append(" assertions")
            }
            append(" failed:\n")

            for ((i, err) in errors.withIndex()) {
                append(i + 1).append(") ").append(err.message).append("\n")
            }
        }
    }
}
