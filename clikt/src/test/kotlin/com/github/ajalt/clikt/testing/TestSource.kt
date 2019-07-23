package com.github.ajalt.clikt.testing

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.fileconfig.CliktValuesSource
import io.kotlintest.shouldBe

class TestSource(vararg values: Pair<String, String>) : CliktValuesSource {
    private var initialized: Boolean = false
    private var read: Boolean = false
    private var closed: Boolean = false
    private val map = values.toMap()

    override fun initialize() {
        initialized = true
    }

    override fun readValues(context: Context, key: String): List<CliktValuesSource.Invocation> {
        read = true
        return map[key]?.let { CliktValuesSource.Invocation.just(it) } ?: emptyList()
    }

    override fun close() {
        closed = true
    }

    fun assert(read: Boolean) {
        initialized shouldBe true
        closed shouldBe true
        this.read shouldBe read
    }
}
