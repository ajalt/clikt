package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.FileParamType
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.io.File

class ParamTypeTest {
    private fun mockFile(name: String = "/", exists: Boolean = true, isFile: Boolean = false,
                         isDirectory: Boolean = false, canWrite: Boolean = true,
                         canRead: Boolean = true): File {
        return mock {
            on { toString() } doReturn name
            on { exists() } doReturn exists
            on { isFile() } doReturn isFile
            on { isDirectory() } doReturn isDirectory
            on { canWrite() } doReturn canWrite
            on { canRead() } doReturn canRead
        }
    }

    @Test
    fun `test FileParamType success`() {
        val f = mockFile()
        val p = FileParamType(true, true, true, true, true)
        p.check(f)
    }

    @Test
    fun `test FileParamType exists`() {
        val f = mockFile(exists = false)
        val p = FileParamType(true, true, true, true, true)
        assertThatThrownBy { p.check(f) }.isInstanceOf(BadParameter::class.java)
    }
}
