package com.github.ajalt.clikt.parsers

import com.github.ajalt.clikt.parameters.options.Option

/**
 * The output of parsing a single option and its values.
 *
 * @param name The name that was used to invoke the option. May be empty if the value was not retrieved
 *   from the command line (e.g. values from environment variables).
 * @param values The values provided to the option. All invocations passed to [Option.finalize]
 *   will have a size in the range of [Option.nvalues].
 */
data class Invocation(val name: String, val values: List<String>)
