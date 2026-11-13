package com.example.presentation.model

import com.example.domain.DataError
import com.example.domain.Error
import com.example.domain.Kind
import com.example.domain.UnexpectedError
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorMappingTest {

    private data class TestDataError(override val kind: Kind, override val message: String?) : DataError

    @Test
    fun `data errors keep their message and kind`() {
        assertEquals(
            UIState.Error(message = "timeout", kind = Kind.NETWORK),
            TestDataError(kind = Kind.NETWORK, message = "timeout").toUiError()
        )
    }

    @Test
    fun `null data error message becomes empty`() {
        assertEquals(
            UIState.Error(message = "", kind = Kind.HTTP),
            TestDataError(kind = Kind.HTTP, message = null).toUiError()
        )
    }

    @Test
    fun `unexpected error maps to UNEXPECTED kind`() {
        assertEquals(
            UIState.Error(message = "oops", kind = Kind.UNEXPECTED),
            UnexpectedError(message = "oops").toUiError()
        )
    }

    @Test
    fun `non data errors map to an empty unexpected error`() {
        val error = object : Error {}

        assertEquals(UIState.Error(message = "", kind = Kind.UNEXPECTED), error.toUiError())
    }
}
