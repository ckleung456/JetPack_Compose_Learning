package com.example.myapplication.features.doordash.usecases

import com.example.core.usecase.FlowUseCase
import com.example.myapplication.features.doordash.model.data.LikedStatus
import com.example.myapplication.features.doordash.model.data.RestaurantDataModel
import com.example.myapplication.features.doordash.model.usecases.GetNearByRestaurantInput
import com.example.myapplication.features.doordash.module.local.DoordashDbRepository
import com.example.myapplication.features.doordash.module.network.DoorDashRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class GetNearByRestaurantsUseCase @Inject constructor(
    private val remoteRepository: DoorDashRepository,
    private val localRepository: DoordashDbRepository
) : FlowUseCase<GetNearByRestaurantInput, List<Pair<RestaurantDataModel, LikedStatus>>>() {
    override suspend fun flowWork(input: GetNearByRestaurantInput): Flow<List<Pair<RestaurantDataModel, LikedStatus>>> =
        remoteRepository.getRestaurantNearBy(
            lat = input.lat,
            lng = input.lng
        ).map { data ->
            data.map { restaurantDataModel ->
                localRepository.getLikedStatusById(restaurantId = restaurantDataModel.id)?.let {
                    Pair(restaurantDataModel, it)
                } ?: Pair(restaurantDataModel, LikedStatus.NO_REF)
            }
        }
}