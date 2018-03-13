package com.github.ajalt.clikt.samples.validation

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import java.net.MalformedURLException
import java.net.URL

class Cli : CliktCommand(help = "Validation examples") {
    val count by option(help = "A positive even number").int()
            .validate {
                if (it != null && (it < 0 || it % 2 != 0)) {
                    throw BadParameterValue("Should be a positive, even integer")
                }
            }
    val biggerCount by option(help="A number larger than --count").int()
    val url by option(help="A URL")
            .convert {
                try {
                    URL(it)
                } catch (err: MalformedURLException) {
                    throw BadParameterValue("Invalid URL")
                }
            }

    override fun run() {
        // You can't refer to another parameter in a validator or converter, since the other parameter might
        // not be set yet. Instead, validate them after parsing.
        if (biggerCount != null && count != null && biggerCount!! <= count!!) {
            throw BadParameterValue("--bigger-count must be larger than --count")
        }
        println("count: $count")
        println("biggerCount: $biggerCount")
        println("url: $url")
    }

}

fun main(args: Array<String>) = Cli().main(args)
