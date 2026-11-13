package com.example.feature.business.usecase

import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.MainDispatcherRule
import com.example.feature.business.business
import com.example.feature.business.model.domain.BuildBusinessSectionsInput
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessFilter
import com.example.feature.business.model.domain.BusinessSectionUi
import com.example.feature.business.model.domain.NearyByBusinessesUi
import com.example.feature.business.model.domain.toBusinessUi
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BuildBusinessSectionsUseCaseTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val useCase = BuildBusinessSectionsUseCase()

    private val tonys = business(id = "p1", name = "tony's").toBusinessUi()
    private val apizza = business(id = "p2", name = "Apizza").toBusinessUi()
    private val brewery = business(id = "b1", name = "Brewery").toBusinessUi()
    private val businesses = NearyByBusinessesUi(
        pizza = listOf(tonys, apizza, tonys).toImmutableList(),
        beer = listOf(brewery).toImmutableList()
    )

    private suspend fun sections(filter: BusinessFilter = BusinessFilter.ALL, query: String = "") =
        (useCase(BuildBusinessSectionsInput(businesses, filter, query)).last()
                as UseCaseOutputWithStatus.Success).result

    @Test
    fun `all filter returns both sections sorted by name without duplicates`() = runTest {
        assertEquals(
            listOf(
                BusinessSectionUi(BusinessCategory.PIZZA, listOf(apizza, tonys).toImmutableList()),
                BusinessSectionUi(BusinessCategory.BEER, listOf(brewery).toImmutableList())
            ),
            sections()
        )
    }

    @Test
    fun `category filter keeps only that section`() = runTest {
        assertEquals(listOf(BusinessCategory.PIZZA), sections(filter = BusinessFilter.PIZZA).map { it.category })
        assertEquals(listOf(BusinessCategory.BEER), sections(filter = BusinessFilter.BEER).map { it.category })
    }

    @Test
    fun `query matches names ignoring case and drops empty sections`() = runTest {
        val result = sections(query = "APIZ")

        assertEquals(listOf(BusinessSectionUi(BusinessCategory.PIZZA, listOf(apizza).toImmutableList())), result)
    }

    @Test
    fun `no matches returns no sections`() = runTest {
        assertEquals(emptyList<BusinessSectionUi>(), sections(query = "sushi"))
    }
}
