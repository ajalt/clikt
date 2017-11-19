package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
private annotation class CustomAnnotation(vararg val names: String)

private fun badF0(@IntOption xx: String) = xx
private fun badF1(@IntOption(nargs = 2) xx: Int) = xx
private fun badF2(@FlagOption xx: Int) = xx
private fun badF3(@IntArgument xx: String) = xx
private fun badF4(@IntArgument(nargs = 2) xx: Int) = xx

class CommandTest {
    companion object {
        private var intArg1 = -1111111

        fun f0() {}

        @CustomAnnotation("--vv")
        fun f1(@CustomAnnotation("--x") x: Int) {
            intArg1 = x
        }

        private val builderBlock: CommandBuilder.() -> Unit = {
            parameter<CustomAnnotation> { anno, param ->
                Option.build(param) {
                    names = anno.names
                    typedOption(IntParamType, 1)
                }
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

    @Test
    fun `option types must match with nargs=1`() {
        assertThatThrownBy { Command.build(::badF0) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `option types must match with nargs greater than 1`() {
        assertThatThrownBy { Command.build(::badF1) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `option types must match with flag option`() {
        assertThatThrownBy { Command.build(::badF2) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `argument types must match with nargs=1`() {
        assertThatThrownBy { Command.build(::badF3) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `argument types must match with nargs greater than 1`() {
        assertThatThrownBy { Command.build(::badF4) }
                .isInstanceOf(IllegalArgumentException::class.java)
    }
}
