package com.example.feature.business.module.network

import com.example.feature.business.model.domain.Business
import com.example.feature.business.model.domain.BusinessCategory
import com.example.feature.business.model.domain.BusinessDetail
import com.example.feature.business.model.domain.Hours
import com.example.feature.business.model.domain.Open
import com.example.feature.business.model.domain.SpecialHour
import com.example.feature.business.model.network.BusinessDetailResponseDto
import com.example.feature.business.model.network.BusinessDto
import com.example.feature.business.model.network.HoursDto
import com.example.feature.business.model.network.OpenDto
import com.example.feature.business.model.network.SpecialHourDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BusinessRepository {
    suspend fun getPizza(location: String): Flow<List<Business>>
    suspend fun getBeer(location: String): Flow<List<Business>>

    suspend fun getBusinessDetail(id: String): Flow<BusinessDetail>
}

class BusinessRepositoryImp @Inject constructor(
    private val api: BusinessApi
): BusinessRepository {
    override suspend fun getBeer(location: String): Flow<List<Business>> = search(
        term = BusinessCategory.BEER.searchTerm,
        location = location
    )

    override suspend fun getPizza(location: String): Flow<List<Business>> = search(
        term = BusinessCategory.PIZZA.searchTerm,
        location = location
    )

    override suspend fun getBusinessDetail(id: String): Flow<BusinessDetail> = withContext(Dispatchers.IO) {
        api.getBusinessDetail(id = id).map { it.toBusinessDetail() }
    }

    private suspend fun search(term: String, location: String) = withContext(Dispatchers.IO) {
        api.searchBusinesses(
            term = term,
            location = location
        ).map { responseDto ->
            responseDto.businesses.map {
                it.toBusiness()
            }
        }
    }

    private fun BusinessDto.toBusiness() = Business(
        id = id,
        name = name.orEmpty(),
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        rating = rating,
        reviewCount = reviewCount,
        price = price?.takeIf { it.isNotBlank() },
        displayAddress = location?.displayAddress?.joinToString(", ").orEmpty(),
        phone = displayPhone?.takeIf { it.isNotBlank() },
        distanceMeters = distance
    )

    private fun BusinessDetailResponseDto.toBusinessDetail() = BusinessDetail(
        id = id,
        name = name.orEmpty(),
        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
        rating = rating ?: 0.0,
        reviewCount = reviewCount ?: -1,
        price = price?.takeIf { it.isNotBlank() },
        displayAddress = location?.displayAddress?.joinToString(", ").orEmpty(),
        phone = displayPhone?.takeIf { it.isNotBlank() },
        distanceMeters = distance,
        categories = categories.orEmpty().map { it.title.orEmpty() },
        isClaimed = isClaimed ?: false,
        isClosed = isClosed ?: false,
        dateOpened = dateOpened?.takeIf { it.isNotBlank() },
        dateClosed = dateClosed?.takeIf { it.isNotBlank() },
        hours = hours?.firstOrNull()?.toHour(),
        specialHours = specialHours.orEmpty().map { it.toSpecialHour() }
            .filter { it.date.isNotBlank() && it.end.isNotBlank() && it.start.isNotBlank() },
        menu = yelpMenuUrl?.takeIf { it.isNotBlank() }
    )

    private fun HoursDto.toHour() = Hours(
        open = open.orEmpty().map { it.toOpen() }
            .filter { it.day != -1 && it.start.isNotBlank() && it.end.isNotBlank() },
        hoursType = hoursType.orEmpty(),
        isOpenNow = isOpenNow ?: false
    )

    private fun OpenDto.toOpen() = Open(
        isOvernight = isOvernight ?: false,
        start = start.orEmpty(),
        end = end.orEmpty(),
        day = day ?: -1
    )

    private fun SpecialHourDto.toSpecialHour() = SpecialHour(
        date = date.orEmpty(),
        end = end.orEmpty(),
        isClosed = isClosed ?: false,
        isOvernight = isOvernight ?: false,
        start = start.orEmpty()
    )
}