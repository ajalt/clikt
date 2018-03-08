package com.github.ajalt.clikt.parameters.options

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parsers.FlagOptionParser
import com.github.ajalt.clikt.parsers.OptionParser

class EagerOption(
        override val help: String,
        override val names: Set<String>,
        private val callback: (Context, EagerOption) -> Unit) : Option {
    override val secondaryNames: Set<String> get() = emptySet()
    override val parser: OptionParser = FlagOptionParser()
    override val metavar: String? get() = null
    override val nargs: Int get() = 0
    override fun finalize(context: Context) {
        callback(context, this)
    }
}
