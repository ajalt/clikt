package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.argument
import com.github.ajalt.clikt.parameters.option
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.File

class FileTypeTest {
    @Test
    fun `file option with default args`() {
        class C : CliktCommand() {
            val x by option("-x", "--xx").file()
            override fun run() {
                assertThat(x).isInstanceOf(File::class.java)
            }
        }

        C().parse(splitArgv("-x."))
    }

    @Test
    fun `file argument with default args`() {
        class C : CliktCommand() {
            val x by argument().file()
            override fun run() {
                assertThat(x).isInstanceOf(File::class.java)
            }
        }

        C().parse(splitArgv("."))
    }
}
