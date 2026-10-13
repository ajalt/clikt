package com.github.ajalt.clikt.parameters.types

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.testing.TestCommand
import com.github.ajalt.clikt.testing.formattedMessage
import com.github.ajalt.clikt.testing.parse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.js.JsName
import kotlin.test.Test

class OptionChoiceTest {
    @[Test JsName("choice_option_strings")]
    fun `choice option strings`() {
        class C : TestCommand() {
            val x: String? by option("-x", "--xx").choice("foo", "bar")
        }

        C().apply {
            parse("-xfoo")
            x shouldBe "foo"
        }

        C().apply {
            parse("--xx=bar")
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse("-x qux") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: qux. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("-x baz") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: baz. Did you mean bar?"

        shouldThrow<BadParameterValue> { C().parse("-x FOO") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: FOO. (choose from foo, bar)"
    }

    @[Test JsName("choice_option_map")]
    fun `choice option map`() {
        class C : TestCommand() {
            val x by option("-x", "--xx")
                .choice("foo" to 1, "bar" to 2)
        }

        C().apply {
            parse("-xfoo")
            x shouldBe 1
        }

        C().apply {
            parse("--xx=bar")
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("-x qux") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: qux. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("-x baz") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: baz. Did you mean bar?"

        shouldThrow<BadParameterValue> { C().parse("-x FOO") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: FOO. (choose from foo, bar)"
    }

    @[Test JsName("choice_option_insensitive")]
    fun `choice option insensitive`() {
        class C : TestCommand() {
            val x by option("-x").choice("foo", "bar", ignoreCase = true)
            val y by option("-y").choice("foo" to 1, "bar" to 2, ignoreCase = true)
        }

        C().apply {
            parse("-xFOO -yFOO")
            x shouldBe "foo"
            y shouldBe 1
        }

        C().apply {
            parse("-xbar -ybAR")
            x shouldBe "bar"
            y shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("-xqux") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: qux. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("-xbaz") }
            .formattedMessage shouldBe "invalid value for -x: invalid choice: baz. Did you mean bar?"
    }

    @[Test JsName("choice_argument_strings")]
    fun `choice argument strings`() {
        class C : TestCommand() {
            val x: String by argument().choice("foo", "bar")
            override fun run_() {
                registeredArguments()[0].name shouldBe "X"
            }
        }

        C().apply {
            parse("foo")
            x shouldBe "foo"
        }

        C().apply {
            parse("bar")
            x shouldBe "bar"
        }

        shouldThrow<BadParameterValue> { C().parse("qux") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: qux. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("baz") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: baz. Did you mean bar?"

        shouldThrow<BadParameterValue> { C().parse("FOO") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: FOO. (choose from foo, bar)"
    }

    @[Test JsName("choice_argument_map")]
    fun `choice argument map`() {
        class C : TestCommand() {
            val x by argument().choice("foo" to 1, "bar" to 2)
            override fun run_() {
                registeredArguments()[0].name shouldBe "X"
            }
        }

        C().apply {
            parse("foo")
            x shouldBe 1
        }

        C().apply {
            parse("bar")
            x shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("qux") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: qux. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("baz") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: baz. Did you mean bar?"

        shouldThrow<BadParameterValue> { C().parse("FOO") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: FOO. (choose from foo, bar)"
    }

    @[Test JsName("choice_argument_insensitive")]
    fun `choice argument insensitive`() {
        class C : TestCommand() {
            val x by argument().choice("foo", "bar", ignoreCase = true)
            val y by argument().choice("foo" to 1, "bar" to 2, ignoreCase = true)
        }

        C().apply {
            parse("FOO FOO")
            x shouldBe "foo"
            y shouldBe 1
        }

        C().apply {
            parse("bar bAR")
            x shouldBe "bar"
            y shouldBe 2
        }

        shouldThrow<BadParameterValue> { C().parse("qux foo") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: qux. (choose from foo, bar)"

        shouldThrow<BadParameterValue> { C().parse("baz foo") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: baz. Did you mean bar?"
    }

    @[Test JsName("choice_typo_suggestions_single_option")]
    fun `choice typo suggestions single option`() {
        class C : TestCommand() {
            val x: String? by option("-p").choice("android", "wasm", "linux")
        }

        shouldThrow<BadParameterValue> { C().parse("-p andriod") }
            .formattedMessage shouldBe "invalid value for -p: invalid choice: andriod. Did you mean android?"
    }

    @[Test JsName("choice_typo_suggestions_multiple_option")]
    fun `choice typo suggestions multiple option`() {
        class C : TestCommand() {
            val x: String? by option("-p").choice("linuxX64", "linuxArm64", "macosX64")
        }

        shouldThrow<BadParameterValue> { C().parse("-p linuxArm") }
            .formattedMessage shouldBe "invalid value for -p: invalid choice: linuxArm. (Possible choices: linuxArm64, linuxX64)"
    }

    @[Test JsName("choice_typo_suggestions_argument")]
    fun `choice typo suggestions argument`() {
        class C : TestCommand() {
            val x: String by argument().choice("android", "wasm", "linux")
        }

        shouldThrow<BadParameterValue> { C().parse("andriod") }
            .formattedMessage shouldBe "invalid value for X: invalid choice: andriod. Did you mean android?"
    }

    @[Test JsName("choice_typo_suggestions_insensitive")]
    fun `choice typo suggestions insensitive`() {
        class C : TestCommand() {
            val x: String? by option("-p").choice("Android", "Wasm", "Linux", ignoreCase = true)
        }

        shouldThrow<BadParameterValue> { C().parse("-p andriod") }
            .formattedMessage shouldBe "invalid value for -p: invalid choice: andriod. Did you mean Android?"

        shouldThrow<BadParameterValue> { C().parse("-p ANDRIOD") }
            .formattedMessage shouldBe "invalid value for -p: invalid choice: ANDRIOD. Did you mean Android?"
    }
}
