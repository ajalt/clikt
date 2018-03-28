package com.github.ajalt.clikt.samples.cp

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.int
import java.io.File

class Cp : CliktCommand(help = "Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.") {
    private val interactive by option("-i", "--interactive", help = "prompt before overwrite").flag()
    private val recursive by option("-r", "--recursive", help = "copy directories recursively").flag()
    private val source by argument().multiple()
    private val dest by argument()

    override fun run() {
        val destFile = File(dest)
        for (fname in source) {
            val sourceFile = File(fname)
            try {
                if (recursive) sourceFile.copyRecursively(destFile)
                else sourceFile.copyTo(destFile)
            } catch (e: FileAlreadyExistsException) {
                if (interactive) {
                    val response = TermUi.confirm("overwrite '$dest'?", default = true)
                    if (response == false) continue
                }
                if (recursive) sourceFile.copyRecursively(destFile, overwrite = true)
                else sourceFile.copyTo(destFile, overwrite = true)
            }
        }
    }
}

fun main(args: Array<String>) = Cp().main(args)
