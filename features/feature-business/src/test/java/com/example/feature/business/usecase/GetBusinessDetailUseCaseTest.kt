package com.example.feature.business.usecase

import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.FakeBusinessRepository
import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.businessDetail
import com.example.feature.business.fakeBusinessLikedStore
import com.example.feature.business.model.domain.toBusinessDetailUi
import com.example.network.RetrofitException
import app.cash.turbine.test
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class GetBusinessDetailUseCaseTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeBusinessRepository(detail = businessDetail(id = "abc"))
    private val likedStore = fakeBusinessLikedStore()
    private val useCase = GetBusinessDetailUseCase(repository, likedStore)

    @Test
    fun `maps the detail to its UI model`() = runTest {
        val outputs = useCase("abc").take(2).toList()

        assertEquals(
            listOf(
                UseCaseOutputWithStatus.Progress(),
                UseCaseOutputWithStatus.Success(businessDetail(id = "abc").toBusinessDetailUi())
            ),
            outputs
        )
        assertEquals(listOf("abc"), repository.requestedDetailIds)
    }

    @Test
    fun `repository errors become failures`() = runTest {
        repository.detailError = RetrofitException.NetworkError(IOException("offline"))

        val failed = useCase("abc").toList().last()

        assertTrue(failed is UseCaseOutputWithStatus.Failed)
        assertEquals(repository.detailError, (failed as UseCaseOutputWithStatus.Failed).error)
    }

    @Test
    fun `re-emits with the new like status without refetching`() = runTest {
        useCase("abc").test {
            assertTrue(awaitItem() is UseCaseOutputWithStatus.Progress)
            assertEquals(false, (awaitItem() as UseCaseOutputWithStatus.Success).result.isLiked)

            likedStore.toggleBusinessLiked("abc")
            assertEquals(true, (awaitItem() as UseCaseOutputWithStatus.Success).result.isLiked)

            likedStore.toggleBusinessLiked("other") // unrelated id: no emission
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(listOf("abc"), repository.requestedDetailIds)
    }
}
