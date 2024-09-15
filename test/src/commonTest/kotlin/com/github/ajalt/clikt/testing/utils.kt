package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.Localization
import com.github.ajalt.clikt.output.ParameterFormatter

val Throwable.formattedMessage: String?
    get() = (this as? UsageError)?.formatMessage(
        context?.localization ?: defaultLocalization,
        ParameterFormatter.Plain
    ) ?: message

internal val defaultLocalization = object : Localization {}

internal fun <T : CliktCommand> T.withEnv(vararg entries: Pair<String, String?>): T {
    return context { readEnvvar = { entries.toMap()[it] } }
}
