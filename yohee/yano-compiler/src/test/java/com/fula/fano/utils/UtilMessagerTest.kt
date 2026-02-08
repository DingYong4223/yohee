package com.fula.fano.utils

import com.fula.fano.utils.UtilMessager
import org.junit.Test
import org.mockito.Mockito.mock
import javax.lang.model.element.Element

/**
 * Unit tests for [UtilMessager].
 */
class UtilMessagerTest {

    @Test(expected = UninitializedPropertyAccessException::class)
    internal fun `reportError fails when uninitialized`() {
        val element = mock(Element::class.java)
        UtilMessager.reportError(element, "test error")
    }

    @Test
    internal fun `reportInfo does not fail when uninitialized`() {
        UtilMessager.reportInfo("test message")
    }
}