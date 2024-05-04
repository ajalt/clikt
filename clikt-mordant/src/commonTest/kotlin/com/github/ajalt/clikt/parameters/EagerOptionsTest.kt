package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.PrintMessage
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.groupSwitch
import com.github.ajalt.clikt.parameters.groups.mutuallyExclusiveOptions
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.TestException
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.blocking.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

@Suppress("BooleanLiteralArgument", "unused")
class EagerOptionsTest {
    @Test
    @JsName("custom_eager_option")
    fun `custom eager option`() = forAll(
        row("", false, false),
        row("--option", true, false),
        row("-p", false, true),
        row("-op", true, true)
    ) { argv, eO, eP ->
        var calledO = false
        var calledP = false

        class C : TestCommand() {
            init {
                eagerOption("-o", "--option") { calledO = true }
                eagerOption(listOf("-p")) { calledP = true }
            }
        }

        with(C()) {
            parse(argv)
            calledO shouldBe eO
            calledP shouldBe eP
        }
    }

    @Test
    @JsName("eager_option_parse_order")
    fun `eager option parse order`() {
        class C : TestCommand(called = false) {
            val o by option().flag().validate { throw TestException("fail") }
        }

        shouldThrow<PrintHelpMessage> { C().parse("-h --o") }
        shouldThrow<PrintHelpMessage> { C().parse("--o -h") }
        shouldThrow<TestException> { C().parse("--o") }
    }

    @Test
    @JsName("eager_option_in_option_group_plain")
    fun `eager option in option group plain`() {
        class G : OptionGroup(name = "g") {
            val x by option("-x", eager = true).flag().validate { throw TestException("fail") }
        }

        class C : TestCommand(called = false) {
            val y by option("-y").flag().validate { fail("fail") }
            val g by G()
        }

        shouldThrow<PrintHelpMessage> { C().parse("-hxy") }
        shouldThrow<TestException> { C().parse("-xyh") }
        shouldThrow<TestException> { C().parse("-yxh") }
    }

    @Test
    @JsName("eager_option_in_option_group_switch")
    fun `eager option in option group switch`() {
        class G : OptionGroup(name = "g") {
            val x by option("-x", eager = true)
        }

        class C : TestCommand(called = false) {
            val g by option().groupSwitch("--a" to G(), "--b" to G())
        }

        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    @JsName("eager_option_in_option_group_cooccurring")
    fun `eager option in option group cooccurring`() {
        class G : OptionGroup(name = "g") {
            val x by option("-x", eager = true)
            val f by option("-f").required()
        }

        class C : TestCommand(called = false) {
            val g by option().groupSwitch("--a" to G(), "--b" to G())
        }

        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    @JsName("eager_option_in_option_group_mutex")
    fun `eager option in option group mutex`() {
        class C : TestCommand(called = false) {
            val g by mutuallyExclusiveOptions(
                option("-x"),
                option("-y", eager = true)
            )
        }

        shouldThrow<IllegalArgumentException> { C() }
    }

    @Test
    @JsName("version_default")
    fun `version default`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        val exception = shouldThrow<PrintMessage> {
            C().parse("--version")
        }
        exception.formattedMessage shouldBe "prog version 1.2.3"
        exception.statusCode shouldBe 0
    }

    @Test
    @JsName("version_custom_message")
    fun `version custom message`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3", names = setOf("--foo")) { "$it bar" }
            }
        }

        shouldThrow<PrintMessage> {
            C().parse("--foo")
        }.formattedMessage shouldBe "1.2.3 bar"
    }

    @Test
    @JsName("multiple_eager_options")
    fun `multiple eager options`() {
        class C : TestCommand(called = false, name = "prog") {
            init {
                versionOption("1.2.3")
            }
        }

        shouldThrow<PrintHelpMessage> {
            C().parse("--help --version")
        }

        shouldThrow<PrintMessage> {
            C().parse("--version --help")
        }.formattedMessage shouldBe "prog version 1.2.3"
    }
}
