package com.github.ajalt.clikt.testing

import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.AbstractThrowableAssert
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import java.io.File
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.atomic.*

fun splitArgv(argv: String): Array<String> {
    return if (argv.isBlank()) emptyArray() else argv.split(" ").toTypedArray()
}

inline fun softly(block: SoftAssertions.() -> Unit) = SoftAssertions().apply { block(); assertAll() }

inline fun <T : Row> parameterized(vararg data: T, addDescription: Boolean = true, block: ForEachSoftAssertions.(T) -> Unit) {
    val softly = ForEachSoftAssertions()

    for ((i, it) in data.withIndex()) {
        val stringData = pprint(it)
        softly.description = "row=$i" + if (addDescription) ", data=$stringData" else ""
        try {
            softly.block(it)
        } catch (exc: Exception) {
            throw AssertionError("failed with $stringData", exc)
        }
    }

    softly.assertAll()
}

inline fun <reified T : Exception> assertThrows(messageContains: String? = null,
                                                crossinline block: () -> Unit)
        : AbstractThrowableAssert<*, out Throwable> {
    val assert = Assertions.assertThatThrownBy { block() }
    assert.isInstanceOf(T::class.java)
    if (messageContains != null) {
        assert.hasMessageContaining(messageContains)
    }
    return assert
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

fun pprint(it: Any?): String = when (it) {
    is ByteArray -> Arrays.toString(it)
    is CharArray -> Arrays.toString(it)
    is ShortArray -> Arrays.toString(it)
    is IntArray -> Arrays.toString(it)
    is LongArray -> Arrays.toString(it)
    is FloatArray -> Arrays.toString(it)
    is DoubleArray -> Arrays.toString(it)
    is BooleanArray -> Arrays.toString(it)
    is Array<*> -> Arrays.toString(it)
    else -> it.toString()
}


@Suppress("HasPlatformType")
class ForEachSoftAssertions(var description: String = "") : SoftAssertions() {
    fun <SELF : AbstractAssert<SELF, ACTUAL>, ACTUAL> AbstractAssert<SELF, ACTUAL>.called(name: String): SELF =
            describedAs("$description: $name")

    override fun assertThat(actual: BigDecimal?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: BigInteger?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Boolean?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: BooleanArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Byte?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: ByteArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: CharArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Char?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Class<*>?) = super.assertThat(actual).describedAs(description)
    override fun <T : Any?> assertThat(actual: MutableIterable<T>?) = super.assertThat(actual).describedAs(description)
    override fun <T : Any?> assertThat(actual: MutableIterator<T>?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Double?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: DoubleArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: File?) = super.assertThat(actual).describedAs(description)
    override fun <RESULT : Any?> assertThat(actual: Future<out RESULT>?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: InputStream?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Float?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: FloatArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: IntArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Int?) = super.assertThat(actual).describedAs(description)
    override fun <T : Any?> assertThat(actual: MutableList<out T>?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Long?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: LongArray?) = super.assertThat(actual).describedAs(description)
    override fun <T : Any?> assertThat(actual: T) = super.assertThat(actual).describedAs(description)
    override fun <T : Any?> assertThat(actual: Array<out T>?) = super.assertThat(actual).describedAs(description)
    override fun <K : Any?, V : Any?> assertThat(actual: MutableMap<K, V>?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Short?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: ShortArray?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: CharSequence?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: String?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Date?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: AtomicBoolean?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: AtomicInteger?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: AtomicIntegerArray?) = super.assertThat(actual).describedAs(description)
    override fun <OBJECT : Any?> assertThat(actual: AtomicIntegerFieldUpdater<OBJECT>?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: AtomicLong?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: AtomicLongArray?) = super.assertThat(actual).describedAs(description)
    override fun <OBJECT : Any?> assertThat(actual: AtomicLongFieldUpdater<OBJECT>?) = super.assertThat(actual).describedAs(description)
    override fun <VALUE : Any?> assertThat(actual: AtomicReference<VALUE>?) = super.assertThat(actual).describedAs(description)
    override fun <ELEMENT : Any?> assertThat(actual: AtomicReferenceArray<ELEMENT>?) = super.assertThat(actual).describedAs(description)
    override fun <FIELD : Any?, OBJECT : Any?> assertThat(actual: AtomicReferenceFieldUpdater<OBJECT, FIELD>?) = super.assertThat(actual).describedAs(description)
    override fun <VALUE : Any?> assertThat(actual: AtomicMarkableReference<VALUE>?) = super.assertThat(actual).describedAs(description)
    override fun <VALUE : Any?> assertThat(actual: AtomicStampedReference<VALUE>?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: Throwable?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: URI?) = super.assertThat(actual).describedAs(description)
    override fun assertThat(actual: URL?) = super.assertThat(actual).describedAs(description)
}
