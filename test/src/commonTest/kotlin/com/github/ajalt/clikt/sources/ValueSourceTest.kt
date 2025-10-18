package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.Option
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ValueSourceTest {
    @Test
    fun `parameter name can be provided to invocation`() {
        class C : TestCommand() {
            @Suppress("unused")
            val theInteger by option("-i").int()
        }

        val sourceWithoutParameterName = object : ValueSource {
            override fun getValues(
                context: Context,
                option: Option
            ): List<ValueSource.Invocation> = ValueSource.Invocation.just(value = "foo")
        }

        val sourceWithParameterName = object : ValueSource {
            override fun getValues(
                context: Context,
                option: Option
            ): List<ValueSource.Invocation> =
                ValueSource.Invocation.just(value = "foo", location = "value_source_option")
        }

        shouldThrow<BadParameterValue> {
            C().apply { configureContext { valueSource = sourceWithoutParameterName } }.parse("")
        }.formattedMessage shouldBe "invalid value: foo is not a valid integer"

        shouldThrow<BadParameterValue> {
            C().apply { configureContext { valueSource = sourceWithParameterName } }.parse("")
        }.formattedMessage shouldBe "invalid value for value_source_option: foo is not a valid integer"
    }
}