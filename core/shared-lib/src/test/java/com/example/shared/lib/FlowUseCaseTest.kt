package com.example.shared.lib

import com.example.domain.DataError
import com.example.domain.Error
import com.example.domain.ErrorExcept
import com.example.domain.Kind
import com.example.domain.UnexpectedError
import com.example.domain.usecase.UseCaseOutputWithStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FlowUseCaseTest {

    private class TestDataError(override val message: String?) : RuntimeException(message), DataError {
        override val kind: Kind = Kind.HTTP
    }

    /** Doubles the input; [work] can be swapped to simulate failures or multiple emissions. */
    private class DoublingUseCase(
        private val fallback: String? = null,
        private val work: (Int) -> Flow<Int> = { flowOf(it * 2) }
    ) : FlowUseCase<Int, Int, String>() {
        override suspend fun flowWork(input: Int): Flow<Int> = work(input)
        override fun isValidInput(input: Int): Boolean = input >= 0
        override suspend fun errorResult(error: Throwable): String? = fallback
        override suspend fun onSucceedDataHandling(intermediate: Int) =
            UseCaseOutputWithStatus.Success("value=$intermediate")
    }

    @Test
    fun `emits progress then mapped success`() = runTest {
        val outputs = DoublingUseCase()(input = 21).toList()

        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Success("value=42")),
            outputs
        )
    }

    @Test
    fun `emits a success for every intermediate value`() = runTest {
        val useCase = DoublingUseCase(work = { flowOf(1, 2, 3) })

        val outputs = useCase(input = 0).toList()

        assertEquals(
            listOf(
                UseCaseOutputWithStatus.Progress(),
                UseCaseOutputWithStatus.Success("value=1"),
                UseCaseOutputWithStatus.Success("value=2"),
                UseCaseOutputWithStatus.Success("value=3")
            ),
            outputs
        )
    }

    @Test
    fun `invalid input fails without emitting progress`() = runTest {
        val outputs = DoublingUseCase()(input = -1).toList()

        assertEquals(1, outputs.size)
        val failed = outputs.single() as UseCaseOutputWithStatus.Failed
        assertEquals(UnexpectedError(message = "Unknown Error"), failed.error)
    }

    @Test
    fun `domain errors are passed through unchanged`() = runTest {
        val error = TestDataError("404 - Not Found")
        val useCase = DoublingUseCase(work = { flow { throw error } })

        val outputs = useCase(input = 1).toList()

        assertTrue(outputs.first() is UseCaseOutputWithStatus.Progress)
        assertSame(error, (outputs.last() as UseCaseOutputWithStatus.Failed).error)
    }

    @Test
    fun `ErrorExcept is unwrapped to the domain error it carries`() = runTest {
        val domainError = object : Error {}
        val useCase = DoublingUseCase(work = { flow { throw ErrorExcept(domainError) } })

        val failed = useCase(input = 1).toList().last() as UseCaseOutputWithStatus.Failed

        assertSame(domainError, failed.error)
    }

    @Test
    fun `other exceptions are wrapped in UnexpectedError keeping their message`() = runTest {
        val useCase = DoublingUseCase(work = { flow { throw IllegalStateException("boom") } })

        val failed = useCase(input = 1).toList().last() as UseCaseOutputWithStatus.Failed

        assertEquals(UnexpectedError(message = "boom"), failed.error)
    }

    @Test
    fun `blank exception message falls back to Unknown Error`() = runTest {
        val useCase = DoublingUseCase(work = { flow { throw IllegalStateException("  ") } })

        val failed = useCase(input = 1).toList().last() as UseCaseOutputWithStatus.Failed

        assertEquals(UnexpectedError(message = "Unknown Error"), failed.error)
    }

    @Test
    fun `failed output carries errorResult fallback`() = runTest {
        val useCase = DoublingUseCase(
            fallback = "cached",
            work = { flow { throw IllegalStateException("boom") } }
        )

        val failed = useCase(input = 1).toList().last() as UseCaseOutputWithStatus.Failed

        assertEquals("cached", failed.failedResult)
    }
}
