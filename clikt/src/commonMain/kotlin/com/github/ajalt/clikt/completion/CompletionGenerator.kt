package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.completion.CompletionCandidates.Custom.ShellType
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Shell
import com.github.ajalt.clikt.parameters.options.FlagOption

object CompletionGenerator {

    fun generateCompletion(command: CliktCommand, shell: Shell): String = when (shell) {
        Shell.FISH -> generateFishCompletion(command)
        Shell.ZSH -> generateBasicCompletion(command, true)
        Shell.BASH -> generateBasicCompletion(command, false)
    }

    private fun generateBasicCompletion(command: CliktCommand, zsh: Boolean = true): String {
        val commandName = command.commandName
        val (isTopLevel, funcName) = commandCompletionFuncName(command)
        val options = command._options.map { Triple(it.names, it.completionCandidates, it.nvalues) }
        val arguments = command._arguments.map { it.name to it.completionCandidates }
        val subcommands = command._subcommands.map { it.commandName }
        val fixedArgNameArray = command._arguments
                .takeWhile { it.nvalues > 0 }
                .flatMap { arg -> (1..arg.nvalues).map { "'${arg.name}'" } }
                .joinToString(" ")
        val varargName = command._arguments.find { it.nvalues < 0 }?.name.orEmpty()
        val paramsWithCandidates = (options.map { o -> o.first.maxByOrNull { it.length }!! to o.second } + arguments)

        if (options.isEmpty() && subcommands.isEmpty() && arguments.isEmpty()) return ""

        return buildString {
            if (isTopLevel) {
                append("""
                |#!/usr/bin/env ${if (zsh) "zsh" else "bash"}
                |# Command completion for $commandName
                |# Generated by Clikt
                |
                |
                """.trimMargin())

                if (zsh) {
                    append("""
                    |autoload bashcompinit
                    |bashcompinit
                    |
                    |
                    """.trimMargin())
                }

                append("""
                |__skip_opt_eq() {
                |    # this takes advantage of the fact that bash functions can write to local
                |    # variables in their callers
                |    (( i = i + 1 ))
                |    if [[ "${'$'}{COMP_WORDS[${'$'}i]}" == '=' ]]; then
                |          (( i = i + 1 ))
                |    fi
                |}
                |
                """.trimMargin())
            }

            // Generate functions for any custom completions
            for ((name, candidate) in paramsWithCandidates) {
                val body = (candidate as? CompletionCandidates.Custom)?.generator?.invoke(ShellType.BASH)
                        ?: continue
                val indentedBody = body.trimIndent().prependIndent("  ")
                append("""
                |
                |${customParamCompletionName(funcName, name)}() {
                |$indentedBody
                |}
                |
                """.trimMargin())
            }

            // Generate the main completion function for this command
            append("""
            |
            |$funcName() {
            |  local i=${if (isTopLevel) "1" else "$" + "1"}
            |  local in_param=''
            |  local fixed_arg_names=($fixedArgNameArray)
            |  local vararg_name='$varargName'
            |  local can_parse_options=1
            |
            |  while [[ ${'$'}{i} -lt ${'$'}COMP_CWORD ]]; do
            |    if [[ ${'$'}{can_parse_options} -eq 1 ]]; then
            |      case "${'$'}{COMP_WORDS[${'$'}i]}" in
            |        --)
            |          can_parse_options=0
            |          (( i = i + 1 ));
            |          continue
            |          ;;
            |
            """.trimMargin())

            for ((names, _, nargs) in options) {
                append("        ")
                names.joinTo(this, "|", postfix = ")\n")
                append("          __skip_opt_eq\n")
                if (nargs > 0) {
                    append("          (( i = i + $nargs ))\n")
                    append("          [[ \${i} -gt COMP_CWORD ]] && in_param='${names.maxByOrNull { it.length }}' || in_param=''\n")
                } else {
                    append("          in_param=''\n")
                }

                append("""
                |          continue
                |          ;;
                |
                """.trimMargin())
            }

            append("""
            |      esac
            |    fi
            |    case "${'$'}{COMP_WORDS[${'$'}i]}" in
            |
            """.trimMargin())

            for ((name, toks) in command.aliases()) {
                append("""
                |      $name)
                |        (( i = i + 1 ))
                |        COMP_WORDS=( "${'$'}{COMP_WORDS[@]:0:i}"
                """.trimMargin())
                toks.joinTo(this, " ", prefix = " ") { "'$it'" }
                append(""" "${'$'}{COMP_WORDS[@]:${'$'}{i}}" )""").append("\n")
                append("        (( COMP_CWORD = COMP_CWORD + ${toks.size} ))\n")

                if (!command.currentContext.allowInterspersedArgs) {
                    append("        can_parse_options=0\n")
                }

                append("        ;;\n")
            }


            for (sub in command._subcommands) {
                append("""
                |      ${sub.commandName})
                |        ${commandCompletionFuncName(sub).second} ${'$'}(( i + 1 ))
                |        return
                |        ;;
                |
                """.trimMargin())
            }

            append("""
            |      *)
            |        (( i = i + 1 ))
            |        # drop the head of the array
            |        fixed_arg_names=("${'$'}{fixed_arg_names[@]:1}")
            |
            """.trimMargin())

            if (!command.currentContext.allowInterspersedArgs) {
                append("        can_parse_options=0\n")
            }

            append("""
            |        ;;
            |    esac
            |  done
            |  local word="${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"
            |
            """.trimMargin())

            if (options.isNotEmpty()) {
                val prefixChars = options.flatMap { it.first }
                        .mapTo(mutableSetOf()) { it.first().toString() }
                        .joinToString("")
                append("""
                |  if [[ "${"$"}{word}" =~ ^[$prefixChars] ]]; then
                |    COMPREPLY=(${'$'}(compgen -W '
                """.trimMargin())
                options.flatMap { it.first }.joinTo(this, " ")
                append("""' -- "${"$"}{word}"))
                |    return
                |  fi
                |
                 """.trimMargin())
            }

            append("""
            |
            |  # We're either at an option's value, or the first remaining fixed size
            |  # arg, or the vararg if there are no fixed args left
            |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{fixed_arg_names[0]}
            |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{vararg_name}
            |
            |  case "${"$"}{in_param}" in
            |
            """.trimMargin())

            for ((name, completion) in paramsWithCandidates) {
                append("""
                |    $name)
                |
                """.trimMargin())
                when (completion) {
                    CompletionCandidates.None -> {
                    }
                    CompletionCandidates.Path -> {
                        append("       COMPREPLY=(\$(compgen -o default -- \"\${word}\"))\n")
                    }
                    CompletionCandidates.Hostname -> {
                        append("       COMPREPLY=(\$(compgen -A hostname -- \"\${word}\"))\n")
                    }
                    CompletionCandidates.Username -> {
                        append("       COMPREPLY=(\$(compgen -A user -- \"\${word}\"))\n")
                    }
                    is CompletionCandidates.Fixed -> {
                        append("      COMPREPLY=(\$(compgen -W '")
                        completion.candidates.joinTo(this, " ")
                        append("' -- \"\${word}\"))\n")
                    }
                    is CompletionCandidates.Custom -> {
                        if (completion.generator(ShellType.BASH) != null) {
                            // redirect stderr to /dev/null, because bash prints a warning that
                            // "compgen -F might not do what you expect"
                            append("       COMPREPLY=(\$(compgen -F ${customParamCompletionName(funcName, name)} 2>/dev/null))\n")
                        }
                    }
                }

                append("      ;;\n")
            }

            if (subcommands.isNotEmpty()) {
                append("""
                |    *)
                |      COMPREPLY=(${"$"}(compgen -W '
                """.trimMargin())
                subcommands.joinTo(this, " ")
                append("""' -- "${"$"}{word}"))
                |      ;;
                |
                """.trimMargin())
            }

            append("""
            |  esac
            |}
            |
            """.trimMargin())

            for (subcommand in command._subcommands) {
                append(generateBasicCompletion(subcommand))
            }

            if (isTopLevel) {
                append("\ncomplete -F $funcName $commandName")
            }
        }
    }

    private fun commandCompletionFuncName(command: CliktCommand): Pair<Boolean, String> {
        val ancestors = generateSequence(command.currentContext) { it.parent }
                .map { it.command.commandName }
                .toList().asReversed()
        val isTopLevel = ancestors.size == 1
        val funcName = ancestors.joinToString("_", prefix = "_").replace('-', '_')
        return isTopLevel to funcName
    }

    private fun customParamCompletionName(commandFuncName: String, name: String): String {
        return "_${commandFuncName}_complete_${Regex("[^a-zA-Z0-9]").replace(name, "_")}"
    }

    private fun generateFishCompletion(command: CliktCommand): String {
        if (command.notHasMinimumAutoCompleteRequirements)
            return ""

        val commandName = command.commandName
        val needingCommand = "__fish_use_subcommand"
        val usingCommand = "__fish_seen_subcommand_from"

        return generateFishCompletionForCommand(
                command = command,
                parentCommand = null,
                rootCommandName = commandName,
                needingCommand = needingCommand,
                usingCommand = usingCommand
        )
    }

    private fun generateFishCompletionForCommand(
            command: CliktCommand,
            parentCommand: CliktCommand?,
            rootCommandName: String,
            needingCommand: String,
            usingCommand: String
    ): String = buildString {
        val isTopLevel = parentCommand == null
        val commandName = command.commandName
        val parentCommandName = parentCommand?.commandName

        val options = command._options //.filterNot { it.hidden }
        val subcommands = command._subcommands

        if (isTopLevel) {
            val subcommandsName = subcommands.joinToString(" ") { it.commandName }

            appendLine("### Declaring all subcommands")
            appendLine("""
                set -l ${commandName}_subcommands '$subcommandsName'
            """.trimIndent())

            appendLine()
            appendLine("### Adding top level options")
        } else {
            appendLine("### Declaring $commandName")
            append("complete -f -c $rootCommandName ")

            if (rootCommandName == parentCommandName) {
                append("-n $needingCommand ")
            } else {
                append("-n $needingCommand $parentCommandName ")
            }

            append("-a $commandName ")

            val help = command.commandHelp.replace("'", "\\'")
            if (help != null) {
                append("-d '${help}'")
            }

            appendLine()
        }

        options.mapNotNull { option ->
            val names = option.names.shortAndLongNames
            if (names.first == null && names.second == null)
                return@mapNotNull null

            val help = option.optionHelp.replace("'", "\\'")
            buildString {
                append("complete -f -c $rootCommandName ")

                if (isTopLevel) {
                    append("-n \"not $usingCommand \$${commandName}_subcommands\" ")
                } else {
                    append("-n \"$usingCommand $commandName\" ")
                }

                if (names.first != null) {
                    append("-l ${names.first} ")
                }

                if (names.second != null) {
                    append("-s ${names.second} ")
                }

                if (option !is FlagOption<*>) {
                    append("--require-parameter ")
                }

                when (val completionCandidate = option.completionCandidates) {
                    is CompletionCandidates.None -> {
                    }
                    is CompletionCandidates.Path -> {
                        append("-a \"(__fish_complete_path)\" ")
                    }
                    is CompletionCandidates.Hostname -> {
                        append("-a \"(__fish_print_hostnames)\" ")
                    }
                    is CompletionCandidates.Username -> {
                        append("-a \"(__fish_complete_users)\" ")
                    }
                    is CompletionCandidates.Fixed -> {
                        val options = completionCandidate.candidates.joinToString(" ")
                        append("-a \"$options\" ")
                    }
                    is CompletionCandidates.Custom -> {
                        // TODO: Implement custom fish completion
                    }
                }

                if (help != null) {
                    append("-d '$help'")
                }
            }
        }.forEach(::appendLine)

        appendLine()

        subcommands.map { subCommand ->
            generateFishCompletionForCommand(
                    command = subCommand,
                    parentCommand = command,
                    rootCommandName = rootCommandName,
                    needingCommand = needingCommand,
                    usingCommand = usingCommand
            )
        }.forEach(::appendLine)
    }

    private val CliktCommand.notHasMinimumAutoCompleteRequirements: Boolean
        get() = _options.isEmpty() && _subcommands.isEmpty() && _arguments.isEmpty()

    private val Set<String>.shortAndLongNames: Pair<String?, String?>
        get() {
            val shortRegex = Regex("^-[a-zA-Z0-9]+")
            val longRegex = Regex("^-{2}[a-zA-Z0-9-_]+")

            val shortName: String? = find { shortRegex matches it }?.replace("-", "")
            val longName: String? = find { longRegex matches it }?.replace("-", "")

            return (longName to shortName)
        }
}
