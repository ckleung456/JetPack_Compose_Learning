package com.example.myapplication.features.doordash.module.network

import com.example.myapplication.features.doordash.model.data.RestaurantDataModel
import com.example.myapplication.features.doordash.model.data.RestaurantDetailDataModel
import com.example.myapplication.features.doordash.model.network.APIConstants
import kotlinx.coroutines.flow.Flow
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DoorDashAPIs {
    @GET(APIConstants.ENDPOINT_RESTAURANT_LIST)
    fun fetchRestaurantNearBy(
        @Query(APIConstants.ADDRESS_LAT) lat: Float,
        @Query(APIConstants.ADDRESS_LNG) lng: Float,
        @Query(APIConstants.OFFSET) offset: Int,
        @Query(APIConstants.LIMIT) limit: Int
    ): Flow<List<RestaurantDataModel>>

    @GET(APIConstants.ENDPOINT_RESTAURANT_DETAIL)
    fun fetchRestaurantDetail(@Path(APIConstants.RESTAURANT_ID) id: Long): Flow<RestaurantDetailDataModel>
}