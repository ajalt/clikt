package com.github.ajalt.clikt.options

import com.github.ajalt.clikt.parser.BadParameter
import com.github.ajalt.clikt.parser.MissingParameter
import com.github.ajalt.clikt.parser.OptionValueProcessor
import java.io.File

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class FileOption(vararg val names: String,
                            val help: String = "",
                            val nargs: Int = 1,
                            val exists: Boolean = false,
                            val fileOkay: Boolean = true,
                            val folderOkay: Boolean = true,
                            val writable: Boolean = false,
                            val readable: Boolean = false)

/**
 * @param name The name to show in the help message. If not given, defaults to the name of the
 *     parameter being annotated.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class FileArgument(val name: String = "",
                              val nargs: Int = 1,
                              val required: Boolean = false,
                              val help: String = "",
                              val exists: Boolean = false,
                              val fileOkay: Boolean = true,
                              val folderOkay: Boolean = true,
                              val writable: Boolean = false,
                              val readable: Boolean = false)

object FileParamType : ParamType<File> {
    override fun convert(value: String) = File(value)
}

fun fileParamValueProcessor(
        exists: Boolean,
        fileOkay: Boolean,
        dirOkay: Boolean,
        writable: Boolean,
        readable: Boolean
): OptionValueProcessor = { context, values ->
    if (required && values.isEmpty()) throw MissingParameter("option", names)
    val name = when {
        fileOkay && !dirOkay -> "File"
        !fileOkay && dirOkay -> "Directory"
        else -> "Path"
    }
    val file = values.last() as File
    if (exists && !file.exists()) throw BadParameter("$name \"$file\" does not exist.")
    if (!fileOkay && file.isFile) throw BadParameter("$name \"$file\" is a file.")
    if (!dirOkay && file.isDirectory) throw BadParameter("$name \"$file\" is a directory.")
    if (writable && !file.canWrite()) throw BadParameter("$name \"$file\" is not writable.")
    if (readable && !file.canRead()) throw BadParameter("$name \"$file\" is not readable.")
    file
}
