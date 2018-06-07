package com.github.ajalt.clikt.testing

fun splitArgv(argv: String): Array<String> {
    return if (argv.isBlank()) emptyArray() else argv.split(" ").toTypedArray()
}

inline fun <T : Row> parameterized(vararg data: T, block: (T) -> Unit) {
    for (it in data) {
        block(it)
    }
}

fun <A> row(a: A) = Row1(a)
fun <A, B> row(a: A, b: B) = Row2(a, b)
fun <A, B, C> row(a: A, b: B, c: C) = Row3(a, b, c)
fun <A, B, C, D> row(a: A, b: B, c: C, d: D) = Row4(a, b, c, d)
fun <A, B, C, D, E> row(a: A, b: B, c: C, d: D, e: E) = Row5(a, b, c, d, e)

interface Row
data class Row1<out A>(val a: A) : Row
data class Row2<out A, out B>(val a: A, val b: B) : Row
data class Row3<out A, out B, out C>(val a: A, val b: B, val c: C) : Row
data class Row4<out A, out B, out C, out D>(val a: A, val b: B, val c: C, val d: D) : Row
data class Row5<out A, out B, out C, out D, out E>(val a: A, val b: B, val c: C, val d: D, val e: E) : Row
