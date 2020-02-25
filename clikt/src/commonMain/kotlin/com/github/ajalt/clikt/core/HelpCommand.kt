package com.github.ajalt.clikt.core

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional

class HelpCommand : CliktCommand(name = "help", help = "Get help for a command") {
  val name by argument("command").optional()

  override fun run() {
    val parent = currentContext.parent?.command ?: error("Register ${HelpCommand::class.simpleName}as a sub-command to some other command!")
    val subcommand = parent._subcommands.firstOrNull { it.commandName == name }
    throw PrintHelpMessage(subcommand ?: parent)
  }
}