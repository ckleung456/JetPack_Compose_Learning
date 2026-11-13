package com.example.feature.doordash.module.domain

import com.example.feature.doordash.model.domain.LikedStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class LikeStatusTypeConverterTest {

    private val converter = LikeStatusTypeConverter()

    @Test
    fun `round trips every status`() {
        LikedStatus.entries.forEach { status ->
            assertEquals(status, converter.intToType(converter.typeToInt(status)))
        }
    }

    @Test
    fun `uses stable column values`() {
        assertEquals(1, converter.typeToInt(LikedStatus.LIKED))
        assertEquals(0, converter.typeToInt(LikedStatus.UN_LIKED))
        assertEquals(-1, converter.typeToInt(LikedStatus.NO_REF))
    }

    @Test
    fun `unknown values map to NO_REF`() {
        assertEquals(LikedStatus.NO_REF, converter.intToType(42))
    }
}
