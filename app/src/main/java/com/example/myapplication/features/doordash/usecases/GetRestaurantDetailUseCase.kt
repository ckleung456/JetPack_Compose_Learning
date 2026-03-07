package com.example.myapplication.features.doordash.usecases

import com.example.core.usecase.FlowUseCase
import com.example.myapplication.features.doordash.model.data.LikedStatus
import com.example.myapplication.features.doordash.model.data.RestaurantDetailDataModel
import com.example.myapplication.features.doordash.model.usecases.GetRestaurantDetailInput
import com.example.myapplication.features.doordash.module.local.DoordashDbRepository
import com.example.myapplication.features.doordash.module.network.DoorDashRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class GetRestaurantDetailUseCase @Inject constructor(
    private val remoteRepository: DoorDashRepository,
    private val localRepository: DoordashDbRepository
) : FlowUseCase<GetRestaurantDetailInput, Pair<RestaurantDetailDataModel, LikedStatus>>() {
    override suspend fun flowWork(input: GetRestaurantDetailInput): Flow<Pair<RestaurantDetailDataModel, LikedStatus>> =
        remoteRepository.getRestaurantDetail(restaurantId = input.restaurantId)
            .map { dataModel ->
                localRepository.getLikedStatusById(restaurantId = input.restaurantId)?.let {
                    Pair(dataModel, it)
                } ?: Pair(dataModel, LikedStatus.NO_REF)
            }
}