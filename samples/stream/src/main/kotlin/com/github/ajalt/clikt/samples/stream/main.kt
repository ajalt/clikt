package com.github.ajalt.clikt.samples.stream

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.defaultStdin
import com.github.ajalt.clikt.parameters.types.inputStream
import java.security.DigestInputStream
import java.security.MessageDigest

class Md5Sum : CliktCommand(help = "Compute MD5 checksum") {

    private val input by option().inputStream().defaultStdin()

    override fun run() {
        val digest = MessageDigest.getInstance("MD5")

        DigestInputStream(input, digest).use { input ->
            var read = input.read()

            while (read != -1) {
                read = input.read()
            }
        }

        val hash = digest.digest()

        val sum = hash.joinToString(separator = "") {
            "%02x".format(it.toInt() and 0xFF)
        }

        echo("md5sum: $sum")
    }
}

fun main(args: Array<String>) = Md5Sum().main(args)
