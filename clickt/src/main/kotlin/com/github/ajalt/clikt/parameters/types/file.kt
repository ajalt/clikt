package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameter
import com.github.ajalt.clikt.parameters.*
import java.io.File

private fun convertToFile(exists: Boolean,
                          fileOkay: Boolean,
                          folderOkay: Boolean,
                          writable: Boolean,
                          readable: Boolean): Pair<String, (String) -> File> {
    val name = when {
        fileOkay && !folderOkay -> "File"
        !fileOkay && folderOkay -> "Directory"
        else -> "Path"
    }

    return name to { path ->
        File(path).also {
            if (exists && !it.exists()) throw BadParameter("$name \"$it\" does not exist.")
            if (!fileOkay && it.isFile) throw BadParameter("$name \"$it\" is a file")
            if (!folderOkay && it.isDirectory) throw BadParameter("$name \"$it\" is a directory.")
            if (writable && !it.canWrite()) throw BadParameter("$name \"$it\" is not writable.")
            if (readable && !it.canRead()) throw BadParameter("$name \"$it\" is not readable.")
        }
    }
}


fun RawArgument.file(exists: Boolean = false,
                     fileOkay: Boolean = true,
                     folderOkay: Boolean = true,
                     writable: Boolean = false,
                     readable: Boolean = false): ProcessedArgument<File, File> {
    val (name, conversion) = convertToFile(exists, fileOkay, folderOkay, writable, readable)
    return convert(name.toUpperCase(), conversion)
}

fun RawOption.file(exists: Boolean = false,
                   fileOkay: Boolean = true,
                   folderOkay: Boolean = true,
                   writable: Boolean = false,
                   readable: Boolean = false): LastOccurrenceOption<File, File> {
    val (name, conversion) = convertToFile(exists, fileOkay, folderOkay, writable, readable)
    return convert(name.toUpperCase(), conversion)
}
