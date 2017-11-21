package com.github.ajalt.clickt.samples.echo

import com.github.ajalt.clikt.options.ClicktCommand
import com.github.ajalt.clikt.options.FlagOption
import com.github.ajalt.clikt.options.StringArgument
import com.github.ajalt.clikt.parser.Command


@ClicktCommand(help = "Echo the STRING(s) to standard output")
fun echo(@FlagOption("-n", help = "do not output the trailing newline") suppressNewline: Boolean,
         @StringArgument(nargs = -1) strings: List<String>) {
    print(strings.joinToString(separator = " ", postfix = if (suppressNewline) "" else "\n"))
}

fun main(args: Array<String>) = Command.build(::echo).main(args)
