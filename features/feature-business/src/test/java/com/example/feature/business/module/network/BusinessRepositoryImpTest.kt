package com.example.feature.business.module.network

import com.example.feature.business.model.domain.Business
import com.example.feature.business.model.domain.Hours
import com.example.feature.business.model.domain.Open
import com.example.feature.business.model.domain.SpecialHour
import com.example.feature.business.model.network.BusinessDetailResponseDto
import com.example.feature.business.model.network.BusinessDto
import com.example.feature.business.model.network.BusinessSearchResponseDto
import com.example.feature.business.model.network.CategoryDto
import com.example.feature.business.model.network.HoursDto
import com.example.feature.business.model.network.LocationDto
import com.example.feature.business.model.network.OpenDto
import com.example.feature.business.model.network.SpecialHourDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BusinessRepositoryImpTest {

    private class FakeBusinessApi(
        val search: BusinessSearchResponseDto = BusinessSearchResponseDto(),
        val detail: BusinessDetailResponseDto = BusinessDetailResponseDto(id = "id")
    ) : BusinessApi {
        val searches = mutableListOf<Pair<String, String>>()

        override fun searchBusinesses(term: String, location: String, limit: Int): Flow<BusinessSearchResponseDto> {
            searches += term to location
            return flowOf(search)
        }

        override fun getBusinessDetail(id: String): Flow<BusinessDetailResponseDto> = flowOf(detail)
    }

    private val dto = BusinessDto(
        id = "1",
        name = "Tony's",
        imageUrl = "https://img",
        reviewCount = 12,
        rating = 4.5,
        price = "$$",
        location = LocationDto(displayAddress = listOf("1 Main St", "San Francisco, CA")),
        displayPhone = "(555) 555-5555",
        distance = 120.0
    )

    @Test
    fun `pizza and beer search with their category terms`() = runTest {
        val api = FakeBusinessApi()
        val repository = BusinessRepositoryImp(api)

        repository.getPizza(location = "SF").first()
        repository.getBeer(location = "LA").first()

        assertEquals(listOf("pizza" to "SF", "beer" to "LA"), api.searches)
    }

    @Test
    fun `maps search results to domain businesses`() = runTest {
        val repository = BusinessRepositoryImp(FakeBusinessApi(search = BusinessSearchResponseDto(listOf(dto))))

        assertEquals(
            listOf(
                Business(
                    id = "1",
                    name = "Tony's",
                    imageUrl = "https://img",
                    rating = 4.5,
                    reviewCount = 12,
                    price = "$$",
                    displayAddress = "1 Main St, San Francisco, CA",
                    phone = "(555) 555-5555",
                    distanceMeters = 120.0
                )
            ),
            repository.getPizza(location = "SF").first()
        )
    }

    @Test
    fun `blank and missing optional fields become null or empty`() = runTest {
        val sparse = BusinessDto(id = "2", name = null, imageUrl = " ", price = "", displayPhone = "")
        val repository = BusinessRepositoryImp(FakeBusinessApi(search = BusinessSearchResponseDto(listOf(sparse))))

        val business = repository.getBeer(location = "SF").first().single()

        assertEquals("", business.name)
        assertEquals(null, business.imageUrl)
        assertEquals(null, business.price)
        assertEquals(null, business.phone)
        assertEquals("", business.displayAddress)
    }

    @Test
    fun `detail mapping applies defaults and drops incomplete hours`() = runTest {
        val detailDto = BusinessDetailResponseDto(
            id = "d1",
            name = "Detail",
            categories = listOf(CategoryDto(title = "Pizza"), CategoryDto(title = null)),
            hours = listOf(
                HoursDto(
                    open = listOf(
                        OpenDto(start = "1100", end = "2200", day = 1),
                        OpenDto(start = "", end = "2200", day = 2),
                        OpenDto(start = "1100", end = "2200", day = null)
                    ),
                    hoursType = "REGULAR",
                    isOpenNow = true
                )
            ),
            specialHours = listOf(
                SpecialHourDto(date = "2026-12-25", start = "1000", end = "1400", isClosed = false),
                SpecialHourDto(date = "", start = "1000", end = "1400")
            ),
            yelpMenuUrl = " "
        )
        val repository = BusinessRepositoryImp(FakeBusinessApi(detail = detailDto))

        val detail = repository.getBusinessDetail(id = "d1").first()

        assertEquals(0.0, detail.rating, 0.0)
        assertEquals(-1, detail.reviewCount)
        assertEquals(listOf("Pizza", ""), detail.categories)
        assertFalse(detail.isClaimed)
        assertEquals(null, detail.menu)
        assertEquals(
            Hours(open = listOf(Open(isOvernight = false, start = "1100", end = "2200", day = 1)), hoursType = "REGULAR", isOpenNow = true),
            detail.hours
        )
        assertEquals(
            listOf(SpecialHour(date = "2026-12-25", end = "1400", isClosed = false, isOvernight = false, start = "1000")),
            detail.specialHours
        )
    }
}
