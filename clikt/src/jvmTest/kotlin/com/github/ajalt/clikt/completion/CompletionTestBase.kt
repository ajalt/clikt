package com.github.ajalt.clikt.completion

import com.github.ajalt.clikt.core.PrintCompletionMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("unused")
abstract class CompletionTestBase(private val envShell: String) {
    @Rule
    @JvmField
    val env = EnvironmentVariables()

    private fun doTest(expected: String, command: TestCommand) {
        env.set("TEST_COMPLETE", envShell)
        val message = shouldThrow<PrintCompletionMessage> {
            command.parse("")
        }.message
        try {
            assertEquals(expected.trimMargin(), message)
        } catch (e: Throwable) {
            println(message)
            throw e
        }
    }

    protected abstract fun `custom completions expected`(): String

    @Test
    fun `custom completions`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            val o by option(completionCandidates = CompletionCandidates.Custom.fromStdout("echo foo bar"))
            val a by argument(completionCandidates = CompletionCandidates.Custom {
                when (envShell) {
                    "fish" -> "(echo zzz xxx)"
                    else -> """
                        WORDS=${'$'}(echo zzz xxx)
                        COMPREPLY=(${'$'}(compgen -W "${'$'}WORDS" -- "${'$'}{COMP_WORDS[${'$'}COMP_CWORD]}"))
                        """.trimIndent()
                }
            })
        }
        doTest(
                `custom completions expected`(),
                C()
        )
    }

    protected abstract fun `subcommands with multi-word names expected`(): String

    @Test
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
                )
        )
    }

    protected abstract fun `option secondary names expected`(): String

    @Test
    fun `option secondary names`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            val flag by option().flag("--no-flag")
        }

        doTest(
                `option secondary names expected`(),
                C()
        )
    }

    protected abstract fun `explicit completion candidates expected`(): String

    @Test
    fun `explicit completion candidates`() {
        class C : TestCommand(autoCompleteEnvvar = "TEST_COMPLETE") {
            init {
                context { helpOptionNames = emptySet() }
            }

            val none by option(completionCandidates = CompletionCandidates.None).file()
            val path by option(completionCandidates = CompletionCandidates.Path)
            val host by option(completionCandidates = CompletionCandidates.Hostname)
            val user by option(completionCandidates = CompletionCandidates.Username)
            val fixed by option(completionCandidates = CompletionCandidates.Fixed("foo", "bar")).file()
            val argUser by argument(completionCandidates = CompletionCandidates.Username).file()
            val argFixed by argument(completionCandidates = CompletionCandidates.Fixed("baz", "qux")).file()
        }

        doTest(
                `explicit completion candidates expected`(),
                C()
        )
    }
}
