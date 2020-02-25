package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.testing.TestCommand
import io.kotest.assertions.throwables.shouldThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class HelpCommandTest {
  @Test
  fun helpCommandWithoutCommand() {
    val command = TestCommand().subcommands(HelpCommand(), TestCommand())
    shouldThrow<PrintHelpMessage> {
      command.parse(listOf("help"))
      command.run()
    }.apply {
      assertEquals(this.command, command, "HelpCommand threw wrong print help")
    }
  }

  @Test
  fun helpCommandAsRootCommand() {
    shouldThrow<IllegalStateException> {
      HelpCommand().main(listOf())
    }
  }

  @Test
  fun helpCommandForSubcommand() {
    val commandToPrintHelp = TestCommand(name = "test")
    val command = TestCommand().subcommands(HelpCommand(), commandToPrintHelp)
    shouldThrow<PrintHelpMessage> {
      command.parse(listOf("help", "test"))
      command.run()
    }.apply {
      assertEquals(this.command, commandToPrintHelp, "HelpCommand threw wrong print help")
    }
  }
}