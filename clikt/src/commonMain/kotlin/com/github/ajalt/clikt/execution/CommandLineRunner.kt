package com.github.ajalt.clikt.execution
//
//import com.github.ajalt.clikt.core.BaseCliktCommand
//import com.github.ajalt.clikt.core.CliktError
//import com.github.ajalt.clikt.mpp.exitProcessMpp
//import com.github.ajalt.clikt.tmp.CommandLineParser
//
//fun BaseCliktCommand<() -> Unit>.parse(argv: Array<String>) {
//    val result = CommandLineParser.parse(this, argv.asList())
//    for (invocation in result.invocations) {
//        CommandLineParser.finalize(invocation)
//        invocation.command.runner()
//    }
//}
//
//fun BaseCliktCommand<() -> Unit>.main(argv: Array<String>) {
//    try {
//        parse(argv)
//    } catch (e: CliktError) {
//        echoFormattedHelp(e)
//        exitProcessMpp(e.statusCode)
//    }
//}
//
//suspend fun BaseCliktCommand<suspend () -> Unit>.parse(argv: Array<String>) {
//    val result = CommandLineParser.parse(this, argv.asList())
//    for (invocation in result.invocations) {
//        CommandLineParser.finalize(invocation)
//        invocation.command.runner()
//    }
//}
//
//fun <T> BaseCliktCommand<(T) -> T>.parse(initialValue: T, argv: Array<String>): T {
//    val result = CommandLineParser.parse(this, argv.asList())
//    var value = initialValue
//    for (invocation in result.invocations) {
//        CommandLineParser.finalize(invocation)
//        value = invocation.command.runner(value)
//    }
//    return value
//}
