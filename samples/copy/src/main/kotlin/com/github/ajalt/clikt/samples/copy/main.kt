package com.github.ajalt.clikt.samples.copy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.mordant.terminal.YesNoPrompt

class Copy : CliktCommand(help = "Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.") {
    val interactive by option("-i", "--interactive", help = "prompt before overwrite").flag()
    val recursive by option("-r", "--recursive", help = "copy directories recursively").flag()
    val source by argument().file(mustExist = true).multiple()
    val dest by argument().file(canBeFile = false)

    override fun run() {
        for (file in source) {
            try {
                if (recursive) file.copyRecursively(dest)
                else file.copyTo(dest)
            } catch (e: FileAlreadyExistsException) {
                if (interactive) {
                    val response = YesNoPrompt("overwrite '$dest'?", terminal, default = true).ask()
                    if (response == false) continue
                }
                if (recursive) file.copyRecursively(dest, overwrite = true)
                else file.copyTo(dest, overwrite = true)
            }
        }
    }
}

fun main(args: Array<String>) = Copy().main(args)
