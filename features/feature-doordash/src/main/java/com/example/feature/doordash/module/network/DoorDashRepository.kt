package com.example.feature.doordash.module.network

import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class DoorDashRepository @Inject constructor(
    private val apiService: DoorDashAPIs
) {
    companion object {
        const val OFFSET = 0
        const val LIMIT = 50
    }

    suspend fun getRestaurantNearBy(
        lat: Float,
        lng: Float
    ): Flow<List<RestaurantDataModel>> = withContext(Dispatchers.IO) {
        apiService.fetchRestaurantNearBy(
            lat = lat,
            lng = lng,
            offset = OFFSET,
            limit = LIMIT
        )
    }

    suspend fun getRestaurantDetail(restaurantId: Long): Flow<RestaurantDetailDataModel> = withContext(Dispatchers.IO) {
        apiService.fetchRestaurantDetail(
            id = restaurantId
        ).map {
            it.copy(id = restaurantId)
        }
    }
}