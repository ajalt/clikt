package com.github.ajalt.clickt.samples.cp

import com.github.ajalt.clikt.options.ClicktCommand
import com.github.ajalt.clikt.options.FlagOption
import com.github.ajalt.clikt.options.StringArgument
import com.github.ajalt.clikt.parser.Command
import com.github.ajalt.clikt.parser.TermUi
import java.io.File


@ClicktCommand(help = "Copy SOURCE to DEST, or multiple SOURCE(s) to directory DEST.")
fun cp(@FlagOption("-i", "--interactive", help = "prompt before overwrite") interactive: Boolean,
       @FlagOption("-r", "--recursive", help = "copy directories recursively") recursive: Boolean,
       @StringArgument(nargs = -1) source: List<String>,
       @StringArgument(required = true) dest: String) {
    val destFile = File(dest)
    for (fname in source) {
        val sourceFile = File(fname)
        try {
            if (recursive) sourceFile.copyRecursively(destFile)
            else sourceFile.copyTo(destFile)
        } catch (e: FileAlreadyExistsException) {
            if (interactive) {
                val response = TermUi.confirm("overwrite '$dest'?", default = true)
                if (!response) continue
            }
            if (recursive) sourceFile.copyRecursively(destFile, overwrite = true)
            else sourceFile.copyTo(destFile, overwrite = true)
        }
    }
}

fun main(args: Array<String>) = Command.build(::cp).main(args)
