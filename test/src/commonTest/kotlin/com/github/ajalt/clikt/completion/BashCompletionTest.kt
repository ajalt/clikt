package com.github.ajalt.clikt.completion


class BashCompletionTest : CompletionTestBase("bash") {
    override fun `custom completions expected`(): String {
        return """
        |#!/usr/bin/env bash
        |# Command completion for c
        |# Generated by Clikt
        |
        |__skip_opt_eq() {
        |    # this takes advantage of the fact that bash functions can write to local
        |    # variables in their callers
        |    (( i = i + 1 ))
        |    if [[ "${'$'}{COMP_WORDS[${'$'}i]}" == '=' ]]; then
        |          (( i = i + 1 ))
        |    fi
        |}
        |
        |__c_complete___o() {
        |  COMPREPLY=(${'$'}(compgen -W "${'$'}(echo foo bar)" -- "${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"))
        |}
        |
        |__c_complete_A() {
        |  WORDS=${'$'}(echo zzz xxx)
        |  COMPREPLY=(${'$'}(compgen -W "${'$'}WORDS" -- "${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"))
        |}
        |
        |_c() {
        |  local i=1
        |  local in_param=''
        |  local fixed_arg_names=('A')
        |  local vararg_name=''
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
        |        --o)
        |          __skip_opt_eq
        |          (( i = i + 1 ))
        |          [[ ${'$'}{i} -gt COMP_CWORD ]] && in_param='--o' || in_param=''
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${'$'}{COMP_WORDS[${'$'}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${'$'}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"
        |  if [[ "${'$'}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${'$'}(compgen -W '--o -h --help' -- "${'$'}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{fixed_arg_names[0]}
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{vararg_name}
        |
        |  case "${'$'}{in_param}" in
        |    "--o")
        |       COMPREPLY=(${'$'}(compgen -F __c_complete___o 2>/dev/null))
        |      ;;
        |    "--help")
        |      ;;
        |    "A")
        |       COMPREPLY=(${'$'}(compgen -F __c_complete_A 2>/dev/null))
        |      ;;
        |  esac
        |}
        |
        |complete -F _c c
        """
    }


    override fun `subcommands with multi-word names expected`(): String {
        return """
        |#!/usr/bin/env bash
        |# Command completion for c
        |# Generated by Clikt
        |
        |__skip_opt_eq() {
        |    # this takes advantage of the fact that bash functions can write to local
        |    # variables in their callers
        |    (( i = i + 1 ))
        |    if [[ "${"$"}{COMP_WORDS[${"$"}i]}" == '=' ]]; then
        |          (( i = i + 1 ))
        |    fi
        |}
        |
        |_c() {
        |  local i=1
        |  local in_param=''
        |  local fixed_arg_names=()
        |  local vararg_name=''
        |  local can_parse_options=1
        |
        |  while [[ ${"$"}{i} -lt ${"$"}COMP_CWORD ]]; do
        |    if [[ ${"$"}{can_parse_options} -eq 1 ]]; then
        |      case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |        --)
        |          can_parse_options=0
        |          (( i = i + 1 ));
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |      sub)
        |        _c_sub ${"$"}(( i + 1 ))
        |        return
        |        ;;
        |      sub-command)
        |        _c_sub_command ${"$"}(( i + 1 ))
        |        return
        |        ;;
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${"$"}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${"$"}{COMP_WORDS[${"$"}COMP_CWORD]}"
        |  if [[ "${"$"}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${"$"}(compgen -W '-h --help' -- "${"$"}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{fixed_arg_names[0]}
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{vararg_name}
        |
        |  case "${"$"}{in_param}" in
        |    "--help")
        |      ;;
        |    *)
        |      COMPREPLY=(${"$"}(compgen -W 'sub sub-command' -- "${"$"}{word}"))
        |      ;;
        |  esac
        |}
        |
        |_c_sub() {
        |  local i=${"$"}1
        |  local in_param=''
        |  local fixed_arg_names=()
        |  local vararg_name=''
        |  local can_parse_options=1
        |
        |  while [[ ${"$"}{i} -lt ${"$"}COMP_CWORD ]]; do
        |    if [[ ${"$"}{can_parse_options} -eq 1 ]]; then
        |      case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |        --)
        |          can_parse_options=0
        |          (( i = i + 1 ));
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${"$"}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${"$"}{COMP_WORDS[${"$"}COMP_CWORD]}"
        |  if [[ "${"$"}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${"$"}(compgen -W '-h --help' -- "${"$"}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{fixed_arg_names[0]}
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{vararg_name}
        |
        |  case "${"$"}{in_param}" in
        |    "--help")
        |      ;;
        |  esac
        |}
        |
        |_c_sub_command() {
        |  local i=${"$"}1
        |  local in_param=''
        |  local fixed_arg_names=()
        |  local vararg_name=''
        |  local can_parse_options=1
        |
        |  while [[ ${"$"}{i} -lt ${"$"}COMP_CWORD ]]; do
        |    if [[ ${"$"}{can_parse_options} -eq 1 ]]; then
        |      case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |        --)
        |          can_parse_options=0
        |          (( i = i + 1 ));
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |      sub-sub)
        |        _c_sub_command_sub_sub ${"$"}(( i + 1 ))
        |        return
        |        ;;
        |      long-sub-command)
        |        _c_sub_command_long_sub_command ${"$"}(( i + 1 ))
        |        return
        |        ;;
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${"$"}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${"$"}{COMP_WORDS[${"$"}COMP_CWORD]}"
        |  if [[ "${"$"}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${"$"}(compgen -W '-h --help' -- "${"$"}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{fixed_arg_names[0]}
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{vararg_name}
        |
        |  case "${"$"}{in_param}" in
        |    "--help")
        |      ;;
        |    *)
        |      COMPREPLY=(${"$"}(compgen -W 'sub-sub long-sub-command' -- "${"$"}{word}"))
        |      ;;
        |  esac
        |}
        |
        |_c_sub_command_sub_sub() {
        |  local i=${"$"}1
        |  local in_param=''
        |  local fixed_arg_names=()
        |  local vararg_name=''
        |  local can_parse_options=1
        |
        |  while [[ ${"$"}{i} -lt ${"$"}COMP_CWORD ]]; do
        |    if [[ ${"$"}{can_parse_options} -eq 1 ]]; then
        |      case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |        --)
        |          can_parse_options=0
        |          (( i = i + 1 ));
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${"$"}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${"$"}{COMP_WORDS[${"$"}COMP_CWORD]}"
        |  if [[ "${"$"}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${"$"}(compgen -W '-h --help' -- "${"$"}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{fixed_arg_names[0]}
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{vararg_name}
        |
        |  case "${"$"}{in_param}" in
        |    "--help")
        |      ;;
        |  esac
        |}
        |
        |_c_sub_command_long_sub_command() {
        |  local i=${"$"}1
        |  local in_param=''
        |  local fixed_arg_names=()
        |  local vararg_name=''
        |  local can_parse_options=1
        |
        |  while [[ ${"$"}{i} -lt ${"$"}COMP_CWORD ]]; do
        |    if [[ ${"$"}{can_parse_options} -eq 1 ]]; then
        |      case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |        --)
        |          can_parse_options=0
        |          (( i = i + 1 ));
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${"$"}{COMP_WORDS[${"$"}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${"$"}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${"$"}{COMP_WORDS[${"$"}COMP_CWORD]}"
        |  if [[ "${"$"}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${"$"}(compgen -W '-h --help' -- "${"$"}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{fixed_arg_names[0]}
        |  [[ -z "${"$"}{in_param}" ]] && in_param=${"$"}{vararg_name}
        |
        |  case "${"$"}{in_param}" in
        |    "--help")
        |      ;;
        |  esac
        |}
        |
        |complete -F _c c
        """
    }

    override fun `option secondary names expected`(): String {
        return """
        |#!/usr/bin/env bash
        |# Command completion for c
        |# Generated by Clikt
        |
        |__skip_opt_eq() {
        |    # this takes advantage of the fact that bash functions can write to local
        |    # variables in their callers
        |    (( i = i + 1 ))
        |    if [[ "${'$'}{COMP_WORDS[${'$'}i]}" == '=' ]]; then
        |          (( i = i + 1 ))
        |    fi
        |}
        |
        |_c() {
        |  local i=1
        |  local in_param=''
        |  local fixed_arg_names=()
        |  local vararg_name=''
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
        |        --flag|--no-flag)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${'$'}{COMP_WORDS[${'$'}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${'$'}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"
        |  if [[ "${'$'}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${'$'}(compgen -W '--flag --no-flag -h --help' -- "${'$'}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{fixed_arg_names[0]}
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{vararg_name}
        |
        |  case "${'$'}{in_param}" in
        |    "--no-flag")
        |      ;;
        |    "--help")
        |      ;;
        |  esac
        |}
        |
        |complete -F _c c
        """
    }


    override fun `explicit completion candidates expected`(): String {
        return """
        |#!/usr/bin/env bash
        |# Command completion for c
        |# Generated by Clikt
        |
        |__skip_opt_eq() {
        |    # this takes advantage of the fact that bash functions can write to local
        |    # variables in their callers
        |    (( i = i + 1 ))
        |    if [[ "${'$'}{COMP_WORDS[${'$'}i]}" == '=' ]]; then
        |          (( i = i + 1 ))
        |    fi
        |}
        |
        |_c() {
        |  local i=1
        |  local in_param=''
        |  local fixed_arg_names=('ARGUSER' 'ARGFIXED')
        |  local vararg_name=''
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
        |        --none)
        |          __skip_opt_eq
        |          (( i = i + 1 ))
        |          [[ ${'$'}{i} -gt COMP_CWORD ]] && in_param='--none' || in_param=''
        |          continue
        |          ;;
        |        --path)
        |          __skip_opt_eq
        |          (( i = i + 1 ))
        |          [[ ${'$'}{i} -gt COMP_CWORD ]] && in_param='--path' || in_param=''
        |          continue
        |          ;;
        |        --host)
        |          __skip_opt_eq
        |          (( i = i + 1 ))
        |          [[ ${'$'}{i} -gt COMP_CWORD ]] && in_param='--host' || in_param=''
        |          continue
        |          ;;
        |        --user)
        |          __skip_opt_eq
        |          (( i = i + 1 ))
        |          [[ ${'$'}{i} -gt COMP_CWORD ]] && in_param='--user' || in_param=''
        |          continue
        |          ;;
        |        --fixed)
        |          __skip_opt_eq
        |          (( i = i + 1 ))
        |          [[ ${'$'}{i} -gt COMP_CWORD ]] && in_param='--fixed' || in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${'$'}{COMP_WORDS[${'$'}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${'$'}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"
        |  if [[ "${'$'}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${'$'}(compgen -W '--none --path --host --user --fixed' -- "${'$'}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{fixed_arg_names[0]}
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{vararg_name}
        |
        |  case "${'$'}{in_param}" in
        |    "--none")
        |      ;;
        |    "--path")
        |       COMPREPLY=(${'$'}(compgen -o default -- "${'$'}{word}"))
        |      ;;
        |    "--host")
        |       COMPREPLY=(${'$'}(compgen -A hostname -- "${'$'}{word}"))
        |      ;;
        |    "--user")
        |       COMPREPLY=(${'$'}(compgen -A user -- "${'$'}{word}"))
        |      ;;
        |    "--fixed")
        |      COMPREPLY=(${'$'}(compgen -W 'foo bar' -- "${'$'}{word}"))
        |      ;;
        |    "ARGUSER")
        |       COMPREPLY=(${'$'}(compgen -A user -- "${'$'}{word}"))
        |      ;;
        |    "ARGFIXED")
        |      COMPREPLY=(${'$'}(compgen -W 'baz qux' -- "${'$'}{word}"))
        |      ;;
        |  esac
        |}
        |
        |complete -F _c c
        """
    }

    override fun `arg names with spaces expected`(): String {
        return """
        |#!/usr/bin/env bash
        |# Command completion for c
        |# Generated by Clikt
        |
        |__skip_opt_eq() {
        |    # this takes advantage of the fact that bash functions can write to local
        |    # variables in their callers
        |    (( i = i + 1 ))
        |    if [[ "${'$'}{COMP_WORDS[${'$'}i]}" == '=' ]]; then
        |          (( i = i + 1 ))
        |    fi
        |}
        |
        |_c() {
        |  local i=1
        |  local in_param=''
        |  local fixed_arg_names=('foo bar')
        |  local vararg_name=''
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
        |        -h|--help)
        |          __skip_opt_eq
        |          in_param=''
        |          continue
        |          ;;
        |      esac
        |    fi
        |    case "${'$'}{COMP_WORDS[${'$'}i]}" in
        |      *)
        |        (( i = i + 1 ))
        |        # drop the head of the array
        |        fixed_arg_names=("${'$'}{fixed_arg_names[@]:1}")
        |        ;;
        |    esac
        |  done
        |  local word="${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"
        |  if [[ "${'$'}{word}" =~ ^[-] ]]; then
        |    COMPREPLY=(${'$'}(compgen -W '-h --help' -- "${'$'}{word}"))
        |    return
        |  fi
        |
        |  # We're either at an option's value, or the first remaining fixed size
        |  # arg, or the vararg if there are no fixed args left
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{fixed_arg_names[0]}
        |  [[ -z "${'$'}{in_param}" ]] && in_param=${'$'}{vararg_name}
        |
        |  case "${'$'}{in_param}" in
        |    "--help")
        |      ;;
        |    "foo bar")
        |      ;;
        |  esac
        |}
        |
        |complete -F _c c
        """
    }
}
