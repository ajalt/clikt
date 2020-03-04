package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.longestName
import com.github.ajalt.clikt.sources.ExperimentalValueSourceApi
import com.github.ajalt.clikt.sources.MapValueSource
import com.github.ajalt.clikt.sources.ValueSource
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalValueSourceApi::class)
class TestSource(vararg values: Pair<String, String>) : ValueSource {
    private var read: Boolean = false
    private val source = MapValueSource(values.toMap())

    override fun getValues(context: Context, option: Option): List<ValueSource.Invocation> {
        read = true
        return source.getValues(context, option)
    }

    fun assert(read: Boolean) {
        this.read shouldBe read
    }
}
