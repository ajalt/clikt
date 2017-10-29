package com.github.ajalt.clikt.parser

import org.junit.Test

import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import org.assertj.core.api.Assertions.assertThat

class PlaintextHelpFormatterTest {
    val formatter = PlaintextHelpFormatter()

    @Test
    fun formatHelp() {
        assertThat(formatter.formatHelp(listOf(ParameterHelp(listOf("--aa", "-a"),
                listOf("INT"), "some thing to live by", 1, false, false)))).isEqualTo(
                """
                |
                |
                |
                |Options:
                |  -a, --aa INT  some thing to live by
                |
                """.trimMargin("|"))
    }
}
