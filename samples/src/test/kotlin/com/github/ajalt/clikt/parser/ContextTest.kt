package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.Context
import com.github.ajalt.clikt.options.PassContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ContextTest {
    companion object {
        fun f1(@PassContext ctx: Context) {
            checkNotNull(ctx)
        }

        fun f2(@PassContext ctx: Context) {
            checkNotNull(ctx)
        }
    }

    @Test
    fun `find functions single context`() {
        val ctx = Command.fromFunction(ContextTest.Companion::f1).makeContext(null)

        assertThat(ctx.findObject<String>()).isNull()
        assertThat(ctx.findRoot()).isEqualTo(ctx)

        assertThat(ctx.findObject { "asd" }).isEqualTo("asd")

        assertThat(ctx.findObject<String>()).isEqualTo("asd")
        assertThat(ctx.findObject<Int>()).isNull()
    }

    @Test
    fun `find functions parent context`() {
        val parent = Command.fromFunction(ContextTest.Companion::f1).makeContext(null)
        val child = Command.fromFunction(ContextTest.Companion::f2).makeContext(parent)

        assertThat(child.findObject<String>()).isEqualTo(parent.findObject<String>()).isNull()
        assertThat(child.findRoot()).isEqualTo(parent.findRoot()).isEqualTo(parent)

        assertThat(parent.findObject { "asd" }).isEqualTo("asd")

        val findObject = child.findObject<String>()
        assertThat(findObject)
                .isEqualTo(parent.findObject<String>())
                .isEqualTo(parent.obj)
                .isEqualTo("asd")

        assertThat(child.obj).isNull()
    }
}
