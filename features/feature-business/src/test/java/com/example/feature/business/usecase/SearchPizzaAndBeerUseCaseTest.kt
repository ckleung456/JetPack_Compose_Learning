package com.example.feature.business.usecase

import com.example.domain.Kind
import com.example.domain.UnexpectedError
import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.FakeBusinessRepository
import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.business
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.NearByBusinessesUi
import com.example.feature.business.model.domain.toBusinessUi
import com.example.network.RetrofitException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SearchPizzaAndBeerUseCaseTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val pizza = business(id = "p1", name = "Pizza Place")
    private val beer = business(id = "b1", name = "Beer Hall")
    private val repository = FakeBusinessRepository(pizza = listOf(pizza), beer = listOf(beer))
    private val useCase = SearchPizzaAndBeerUseCase(repository)

    @Test
    fun `combines pizza and beer results`() = runTest {
        val outputs = useCase(input = "SF").toList()

        assertEquals(
            listOf(
                UseCaseOutputWithStatus.Progress(),
                UseCaseOutputWithStatus.Success(
                    NearByBusinessesUi(
                        pizza = persistentListOf(pizza.toBusinessUi()),
                        beer = persistentListOf(beer.toBusinessUi()),
                        failedCategories = persistentSetOf()
                    )
                )
            ),
            outputs
        )
        assertEquals(listOf("SF"), repository.requestedLocations)
    }

    @Test
    fun `one failed category still returns the other`() = runTest {
        repository.beerError = IOException("beer down")

        val result = (useCase(input = "SF").toList().last() as UseCaseOutputWithStatus.Success).result

        assertEquals(persistentListOf(pizza.toBusinessUi()), result.pizza)
        assertTrue(result.beer.isEmpty())
        assertEquals(setOf(BusinessCategory.BEER), result.failedCategories)
    }

    @Test
    fun `fails when both categories fail`() = runTest {
        repository.pizzaError = IOException("pizza down")
        repository.beerError = IOException("beer down")

        val failed = useCase(input = "SF").toList().last() as UseCaseOutputWithStatus.Failed

        val error = failed.error as RetrofitException.UnexpectedError
        assertEquals(Kind.UNEXPECTED, error.kind)
        assertEquals("pizza down", error.message)
    }

    @Test
    fun `blank location is rejected without hitting the repository`() = runTest {
        val outputs = useCase(input = "  ").toList()

        assertEquals(1, outputs.size)
        assertEquals(UnexpectedError("Unknown Error"), (outputs.single() as UseCaseOutputWithStatus.Failed).error)
        assertTrue(repository.requestedLocations.isEmpty())
    }
}
