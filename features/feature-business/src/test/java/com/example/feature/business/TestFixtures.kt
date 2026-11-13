package com.example.feature.business

import com.example.domain.Result
import com.example.domain.location.LocationAddressProvider
import com.example.domain.location.LocationError
import com.example.feature.business.model.domain.Business
import com.example.feature.business.model.domain.BusinessDetail
import com.example.feature.business.module.network.BusinessRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.util.Locale

/** Swaps [Dispatchers.Main] for a test dispatcher and pins the default locale so number formatting is stable. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    private lateinit var previousLocale: Locale

    override fun starting(description: Description) {
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
        Locale.setDefault(previousLocale)
    }
}

fun business(
    id: String,
    name: String = "Business $id",
    rating: Double = 4.0,
    distanceMeters: Double? = null
) = Business(
    id = id,
    name = name,
    imageUrl = null,
    rating = rating,
    reviewCount = 10,
    price = "$$",
    displayAddress = "1 Main St",
    phone = null,
    distanceMeters = distanceMeters
)

fun businessDetail(id: String = "id-1") = BusinessDetail(
    id = id,
    name = "Detail $id",
    imageUrl = null,
    rating = 4.25,
    reviewCount = 3,
    price = null,
    displayAddress = "1 Main St",
    phone = "(555) 555-5555",
    distanceMeters = null,
    categories = listOf("Pizza"),
    isClaimed = true,
    isClosed = false,
    dateOpened = null,
    dateClosed = null,
    hours = null,
    specialHours = emptyList(),
    menu = null
)

class FakeBusinessRepository(
    var pizza: List<Business> = emptyList(),
    var beer: List<Business> = emptyList(),
    var detail: BusinessDetail = businessDetail()
) : BusinessRepository {
    var pizzaError: Throwable? = null
    var beerError: Throwable? = null
    var detailError: Throwable? = null

    val requestedLocations = mutableListOf<String>()
    val requestedDetailIds = mutableListOf<String>()

    override fun getPizza(location: String): Flow<List<Business>> {
        requestedLocations += location
        return emitOrThrow(pizza, pizzaError)
    }

    override fun getBeer(location: String): Flow<List<Business>> = emitOrThrow(beer, beerError)

    override fun getBusinessDetail(id: String): Flow<BusinessDetail> {
        requestedDetailIds += id
        return emitOrThrow(detail, detailError)
    }

    private fun <T> emitOrThrow(value: T, error: Throwable?): Flow<T> = flow {
        if (error != null) throw error
        emit(value)
    }
}

class FakeLocationAddressProvider(
    var result: Result<String, LocationError> = Result.Success("500 Market St, San Francisco, CA")
) : LocationAddressProvider {
    override suspend fun getCurrentAddress(): Result<String, LocationError> = result
}
