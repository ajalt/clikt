package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.UsageError

val Throwable.formattedMessage: String? get() = (this as? UsageError)?.formatMessage() ?: message

/**
 * As of Kotlin 1.7.20, Kotlin/JS IR generates incorrect code for exception subclasses
 * https://youtrack.jetbrains.com/issue/KT-43490
 *
 * This manifests as "TypeError: Cannot set property message of Error which has only a getter"
 */
expect val skipDueToKT43490: Boolean
