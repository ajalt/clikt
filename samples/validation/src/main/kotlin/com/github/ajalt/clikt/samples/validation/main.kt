package com.github.ajalt.clikt.samples.validation

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.output.TermUi
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.transformAll
import com.github.ajalt.clikt.parameters.options.transformValues
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import java.net.URL

data class Quad(val a: Int, val b: Int, val c: Int, val d: Int)

class Cli : CliktCommand(help = "Validation examples") {
    val count by option(help = "A positive even number").int()
            .validate {
                require(it > 0 && it % 2 == 0) {
                    "Should be a positive, even integer"
                }
            }

    val biggerCount by option(help = "A number larger than --count")
            .int()

    val quad by option(help = "A four-valued option")
            .int()
            .transformValues(4) { Quad(it[0], it[1], it[2], it[3]) }

    val sum by option(help = "All values will be added")
            .int()
            .transformAll { it.sum() }

    val url by argument(help = "A URL")
            .convert { URL(it) }

    override fun run() {
        // You can't refer to another parameter in a validator or converter, since the other parameter might
        // not be set when they're called. Instead, validate them after parsing.
        if (biggerCount != null && count != null && biggerCount!! <= count!!) {
            throw BadParameterValue("--bigger-count must be larger than --count")
        }
        echo("count: $count")
        echo("biggerCount: $biggerCount")
        echo("quad: $quad")
        echo("sum: $sum")
        echo("url: $url")
    }

}

fun main(args: Array<String>) = Cli().main(args)
