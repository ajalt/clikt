package com.github.ajalt.clikt.parameters.transform

import com.github.ajalt.clikt.core.Context

interface TransformContext {
    /** The current context object */
    val context: Context

    /** Throw an exception indicating that usage was incorrect. */
    fun fail(message: String): Nothing

    /** Issue a message that can be shown to the user */
    fun message(message: String)
}
