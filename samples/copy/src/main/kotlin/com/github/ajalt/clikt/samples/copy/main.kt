package com.github.ajalt.clikt.samples.copy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.transformValues
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.file
class OptionGroupModuleB : OptionGroup() {
    val enableModuleB by option().boolean()
        .transformValues(0..0) { true }
        .required()
}
class Copy : CliktCommand(help = "Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.") {
//    val interactive by option("-i", "--interactive", help = "prompt before overwrite").flag()
//    val recursive by option("-r", "--recursive", help = "copy directories recursively").flag()
//    val source by argument().file(mustExist = true).multiple()
//    val dest by argument().file(canBeFile = false)
    val g by OptionGroupModuleB().cooccurring()
    override fun run() {
        echo(g?.enableModuleB)
    }
}



fun main(args: Array<String>) = Copy().main(args)
