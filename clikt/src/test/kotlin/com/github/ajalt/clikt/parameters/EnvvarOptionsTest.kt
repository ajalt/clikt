package com.github.ajalt.clikt.parameters

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.testing.parameterized
import com.github.ajalt.clikt.testing.row
import com.github.ajalt.clikt.testing.splitArgv
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties
import java.io.File


class EnvvarOptionsTest {
    @Rule
    @JvmField
    val env = EnvironmentVariables()

    @Rule
    @JvmField
    val restoreSystemProperties = RestoreSystemProperties()

    @Test
    fun `explicit envvar`() {
        env["FO"] = "foo"

        class C : CliktCommand() {
            val foo by option(envvar = "FO")
            val bar by option()
            override fun run() {
                assertThat(foo).isEqualTo("foo")
                assertThat(bar).isNull()
            }
        }

        C().parse(emptyArray())
    }

    @Test
    fun `auto envvar`() {
        env["FO"] = "foo"
        env["FO"] = "foo"
        env["C_BAR"] = "11"

        class C : CliktCommand(autoEnvvarPrefix = "C") {
            val foo by option(envvar = "FO")
            val bar by option().int()
            val baz by option()
            override fun run() {
                assertThat(foo).isEqualTo("foo")
                assertThat(bar).isEqualTo(11)
                assertThat(baz).isNull()
            }
        }

        C().parse(emptyArray())
    }

    @Test
    fun `auto envvar subcommand`() {
        env["FOO"] = "foo"
        env["C_CMD1_BAR"] = "bar"
        env["BAZ"] = "baz"
        env["CMD2_QUX"] = "qux"
        env["CMD2_SUB3_QUZ"] = "quz"

        class C : CliktCommand(autoEnvvarPrefix = "C") {
            override fun run() = Unit
        }

        class Sub : CliktCommand(name = "cmd1") {
            val foo by option(envvar = "FOO")
            val bar by option()
            override fun run() {
                assertThat(foo).isEqualTo("foo")
                assertThat(bar).isEqualTo("bar")
            }
        }

        class Sub2 : CliktCommand(autoEnvvarPrefix = "CMD2") {
            val baz by option(envvar = "BAZ")
            val qux by option()
            override fun run() {
                assertThat(baz).isEqualTo("baz")
                assertThat(qux).isEqualTo("qux")
            }
        }

        class Sub3 : CliktCommand() {
            val quz by option()
            override fun run() {
                assertThat(quz).isEqualTo("quz")
            }
        }

        C().subcommands(Sub().subcommands(Sub2().subcommands(Sub3())))
                .parse(splitArgv("cmd1 sub2 sub3"))
    }

    @Test
    fun `file envvar`() {
        env["FOO"] = "/home"

        class C : CliktCommand() {
            val foo by option(envvar = "FOO").file()
            val bar by option(envvar = "BAR").file().multiple()
            override fun run() {
                assertThat(foo).isEqualTo(File("/home"))
                assertThat(bar).containsExactly(File("/bar"), File("/baz"))
            }
        }

        System.setProperty("os.name", "Microsoft Windows 10 PRO")
        env["BAR"] = "/bar;/baz"
        C().parse(emptyArray())

        System.setProperty("os.name", "OpenBSD")
        env["BAR"] = "/bar:/baz"
        C().parse(emptyArray())
    }

    @Test
    fun `flag envvars`() = parameterized(
            row(null, null, false, 0),
            row("true", "3", true, 3),
            row("false", "5", false, 5)) { (fv, bv, ef, eb) ->
        env["FOO"] = fv
        env["BAR"] = bv

        var called1 = false
        var called2 = false

        class C : CliktCommand() {
            val foo by option(envvar = "FOO").flag("--no-foo").validate { called1 = true }
            val bar by option(envvar = "BAR").counted().validate { called2 = true }
            override fun run() {
                assertThat(foo).isEqualTo(ef)
                assertThat(bar).isEqualTo(eb)
            }
        }

        C().parse(emptyArray())
        assertThat(called1).isTrue
        assertThat(called2).isTrue
    }
}
