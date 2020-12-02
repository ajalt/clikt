package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import kotlin.js.JsName
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("unused")
abstract class CompletionTestBase(private val shell: String) {

    private fun doTest(expected: String, command: TestCommand) {
        val message = shouldThrow<PrintCompletionMessage> {
            command.parse("--generate-completion=$shell")
        }.message
        try {
            assertEquals(expected.trimMargin(), message)
        } catch (e: Throwable) {
            println(message)
            throw e
        }
    }

    @JsName("custom_completions_expected")
    protected abstract fun `custom completions expected`(): String

    @Test
    @JsName("custom_completions")
    fun `custom completions`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            val o by option(completionCandidates = CompletionCandidates.Custom.fromStdout("echo foo bar"))
            val a by argument(completionCandidates = CompletionCandidates.Custom {
                when (shell) {
                    "fish" -> "\"(echo zzz xxx)\""
                    else -> """
                        WORDS=${'$'}(echo zzz xxx)
                        COMPREPLY=(${'$'}(compgen -W "${'$'}WORDS" -- "${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"))
                        """.trimIndent()
                }
            })
        }
        doTest(
            `custom completions expected`(),
            C().completionOption(hidden = true)
        )
    }

    @JsName("subcommands_with_multi_word_names_expected")
    protected abstract fun `subcommands with multi-word names expected`(): String

    @Test
    @JsName("subcommands_with_multi_word_names")
    fun `subcommands with multi-word names`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE")
        class Sub : TestCommand()
        class SubCommand : TestCommand(name = "sub-command")
        class SubSub : TestCommand()
        class LongSubCommand : TestCommand(name = "long-sub-command")

        doTest(
            `subcommands with multi-word names expected`(),
            C().subcommands(
                Sub(),
                SubCommand().subcommands(SubSub(), LongSubCommand())
            ).completionOption(hidden = true)
        )
    }

    @JsName("option_secondary_names_expected")
    protected abstract fun `option secondary names expected`(): String

    @Test
    @JsName("option_secondary_names")
    fun `option secondary names`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            val flag by option().flag("--no-flag")
        }

        doTest(
            `option secondary names expected`(),
            C().completionOption(hidden = true)
        )
    }

    @JsName("explicit_completion_candidates_expected")
    protected abstract fun `explicit completion candidates expected`(): String

    @Test
    @JsName("explicit_completion_candidates")
    fun `explicit completion candidates`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            init {
                context { helpOptionNames = emptySet() }
            }

            val none by option(completionCandidates = CompletionCandidates.None)
            val path by option(completionCandidates = CompletionCandidates.Path)
            val host by option(completionCandidates = CompletionCandidates.Hostname)
                .convert(completionCandidates = CompletionCandidates.Path) { it }
            val user by option(completionCandidates = CompletionCandidates.Username)
            val fixed by option(completionCandidates = CompletionCandidates.Fixed("foo", "bar"))
            val argUser by argument(completionCandidates = CompletionCandidates.Username)
            val argFixed by argument(completionCandidates = CompletionCandidates.Fixed("baz", "qux"))
        }

        doTest(
            `explicit completion candidates expected`(),
            C().completionOption(hidden = true)
        )
    }

    @Test
    @JsName("completion_command")
    fun `completion command`() {
        shouldThrow<PrintCompletionMessage> {
            TestCommand().subcommands(CompletionCommand()).parse("generate-completion $shell")
        }.message shouldContain shell
    }
}
