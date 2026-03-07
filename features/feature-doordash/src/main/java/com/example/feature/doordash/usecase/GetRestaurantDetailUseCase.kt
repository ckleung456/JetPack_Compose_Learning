package com.example.feature.doordash.usecase

import com.example.core.usecase.FlowUseCase
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.model.domain.usecase.GetRestaurantDetailInput
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
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