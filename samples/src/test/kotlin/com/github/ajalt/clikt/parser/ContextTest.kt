package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.PassContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ContextTest {
    companion object {
        fun f1(@PassContext ctx: Context) {

        }
    }

    @Test
    fun `find functions single context`() {
        val ctx = Context(null, "", null, emptyArray(), true, ContextTest.Companion::f1,
                emptyMap(), emptyMap(), emptyList(), hashSetOf(), emptyMap())

        assertThat(ctx.findObject<String>()).isNull()
        assertThat(ctx.findRoot()).isEqualTo(ctx)

        assertThat(ctx.findObject { "asd" }).isEqualTo("asd")

        assertThat(ctx.findObject<String>()).isEqualTo("asd")
        assertThat(ctx.findObject<Int>()).isNull()
    }

    @Test
    fun `find functions parent context`() {
        val parent = Context(null, "parent", null, emptyArray(), true, ContextTest.Companion::f1,
                emptyMap(), emptyMap(), emptyList(), hashSetOf(), emptyMap())
        val child = Context(parent, "child", null, emptyArray(), true, ContextTest.Companion::f1,
                emptyMap(), emptyMap(), emptyList(), hashSetOf(), emptyMap())

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
