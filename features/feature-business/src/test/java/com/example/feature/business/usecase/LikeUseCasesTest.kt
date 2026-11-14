package com.example.feature.business.usecase

import app.cash.turbine.test
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.fakeBusinessLikedStore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LikeUseCasesTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val store = fakeBusinessLikedStore()
    private val toggle = ToggleBusinessLikeUseCase(store)
    private val observe = ObserveLikedBusinessIdsUseCase(store)

    @Test
    fun `toggle emits the new liked state once and completes`() = runTest {
        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Success(true)),
            toggle("a").toList()
        )
        assertEquals(
            listOf(UseCaseOutputWithStatus.Progress(), UseCaseOutputWithStatus.Success(false)),
            toggle("a").toList()
        )
    }

    @Test
    fun `toggle updates the store`() = runTest {
        toggle("a").toList()

        assertEquals(setOf("a"), store.businessLikedIds.value)
    }

    @Test
    fun `observe emits the current ids then every change`() = runTest {
        store.toggleBusinessLiked("saved")

        observe(Unit).test {
            assertTrue(awaitItem() is UseCaseOutputWithStatus.Progress)
            assertEquals(setOf("saved"), (awaitItem() as UseCaseOutputWithStatus.Success).result)

            toggle("a").toList()
            assertEquals(setOf("saved", "a"), (awaitItem() as UseCaseOutputWithStatus.Success).result)

            toggle("saved").toList()
            assertEquals(setOf("a"), (awaitItem() as UseCaseOutputWithStatus.Success).result)
        }
    }

    @Test
    fun `observers in different use case instances share the same likes`() = runTest {
        // Each ViewModel gets its own use case instances; the singleton store links them.
        val otherScreenToggle = ToggleBusinessLikeUseCase(store)

        observe(Unit).test {
            skipItems(2) // Progress + initial empty set
            otherScreenToggle("a").toList()
            assertEquals(setOf("a"), (awaitItem() as UseCaseOutputWithStatus.Success).result)
        }
    }
}
