package com.example.feature.doordash.usecase

import com.example.core.usecase.FlowUseCase
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDataModel
import com.example.feature.doordash.model.domain.usecase.GetNearByRestaurantInput
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
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