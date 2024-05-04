package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option

/**
 * The [CliktCommand] is the core of command line interfaces in Clikt.
 *
 * Command line interfaces created by creating a subclass of [CliktCommand] with properties defined with
 * [option] and [argument]. You can then parse `argv` by calling [main], which will take care of printing
 * errors and help to the user. If you want to handle output yourself, you can use [parse] instead.
 *
 * Once the command line has been parsed and all the parameters are populated, [run] is called.
 */
abstract class CliktCommand(
    /**
     * The name of the program to use in the help output. If not given, it is inferred from the
     * class name.
     */
    name: String? = null,
) : RunnableCliktCommand(name) {
    init {
        installMordant()
    }
}
