package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.Context

/**
 * Process the parsed values for an [Option] and return the value to call the command with.
 *
 * @see Parameter.processValues
 */
typealias OptionValueProcessor = Option.(Context, List<*>) -> Any?
