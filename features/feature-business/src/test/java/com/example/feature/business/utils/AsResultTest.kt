package com.example.feature.business.utils

import com.example.feature.business.utils.Utils.asResult
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AsResultTest {

    @Test
    fun `wraps values in success and terminates with a failure on error`() = runTest {
        val error = IllegalStateException("boom")
        val results = flow {
            emit(1)
            throw error
        }.asResult().toList()

        assertEquals(listOf(Result.success(1), Result.failure<Int>(error)), results)
    }
}
