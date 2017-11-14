package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.IntParamType
import com.github.ajalt.clikt.options.TypedOptionParser
import com.github.ajalt.clikt.options.VersionOption
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
private annotation class CustomAnnotation(vararg val names: String)

class CommandTest {
    companion object {
        private var intArg1 = -1111111

        fun f0() {}

        @CustomAnnotation("--vv")
        fun f1(@CustomAnnotation("--x") x: Int) {
            intArg1 = x
        }

        private val builderBlock: CommandBuilder.() -> Unit = {
            parameter<CustomAnnotation> { anno, _ ->
                val parser = TypedOptionParser(IntParamType, 1)
                Option(anno.names.toList(), parser, false, -1, "INT", "")
            }

            functionAnnotation<CustomAnnotation> { param ->
                VersionOption(param.names.toList(), "foo", "0.0", "")
            }
        }
    }

    @Before
    fun setup() {
        intArg1 = -1111111
    }

    @Test
    fun `custom parameter annotation`() {
        Command.build(Companion::f1, builderBlock).parse(arrayOf("--x=123"))
        assertThat(intArg1).isEqualTo(123)
    }

    @Test
    fun `custom function annotation`() {
        val command = Command.build(Companion::f1, builderBlock)
        assertThatThrownBy { command.parse(arrayOf("--vv")) }
                .isInstanceOf(PrintMessage::class.java)
                .hasMessage("foo, version 0.0")
    }

    @Test
    fun `custom parameter annotation subcommand function`() {
        Command.build(Companion::f0) {
            subcommand(Companion::f1)
            builderBlock()
        }.parse(arrayOf("f1", "--x=123"))
        assertThat(intArg1).isEqualTo(123)
    }

    @Test
    fun `custom function annotation subcommand function`() {
        val command = Command.build(Companion::f0) {
            subcommand(Companion::f1)
            builderBlock()
        }
        assertThatThrownBy { command.parse(arrayOf("f1", "--vv")) }
                .isInstanceOf(PrintMessage::class.java)
                .hasMessage("foo, version 0.0")
    }
}
