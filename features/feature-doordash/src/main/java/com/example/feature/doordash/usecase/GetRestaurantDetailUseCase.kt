package com.example.feature.doordash.usecase

import com.example.core.usecase.FlowUseCase
import com.example.core.usecase.UseCaseOutputWithStatus
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.RestaurantDetailDataModel
import com.example.feature.doordash.model.domain.usecase.GetRestaurantDetailInput
import com.example.feature.doordash.module.domain.DoordashDbRepository
import com.example.feature.doordash.module.network.DoorDashRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GetRestaurantDetailUseCase @Inject constructor(
    private val remoteRepository: DoorDashRepository,
    private val localRepository: DoordashDbRepository
) : FlowUseCase<GetRestaurantDetailInput, RestaurantDetailDataModel, Pair<RestaurantDetailDataModel, LikedStatus>>() {
    lateinit var input: GetRestaurantDetailInput

    override suspend fun flowWork(input: GetRestaurantDetailInput): Flow<RestaurantDetailDataModel> {
        this.input= input
        return remoteRepository.getRestaurantDetail(restaurantId = input.restaurantId)
    }

    override suspend fun onSucceedDataHandling(intermediate: RestaurantDetailDataModel): UseCaseOutputWithStatus.Success<Pair<RestaurantDetailDataModel, LikedStatus>> =
        UseCaseOutputWithStatus.Success(
            result = localRepository.getLikedStatusById(restaurantId = input.restaurantId)?.let {
                Pair(intermediate, it)
            } ?: run {
                Pair(intermediate, LikedStatus.NO_REF)
            }
        )
}