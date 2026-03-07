package com.example.feature.doordash.module.network

import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class DoorDashRepository @Inject constructor(
    private val apiService: DoorDashAPIs
) {
    companion object {
        const val OFFSET = 0
        const val LIMIT = 50
    }

    fun getRestaurantNearBy(
        lat: Float,
        lng: Float
    ): Flow<List<RestaurantDataModel>> = apiService.fetchRestaurantNearBy(
        lat = lat,
        lng = lng,
        offset = OFFSET,
        limit = LIMIT
    )

    fun getRestaurantDetail(restaurantId: Long): Flow<RestaurantDetailDataModel> =
        apiService.fetchRestaurantDetail(
            id = restaurantId
        )
}