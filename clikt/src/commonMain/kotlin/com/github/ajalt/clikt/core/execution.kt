package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.mpp.exitProcessMpp

// TODO: docs
fun <RunnerT : Function<*>> BaseCliktCommand<RunnerT>.main(
    argv: List<String>,
    parse: BaseCliktCommand<RunnerT>.(List<String>) -> Unit,
) {
    try {
        parse(argv)
    } catch (e: CliktError) {
        echoFormattedHelp(e)
        exitProcessMpp(e.statusCode)
    }
}
