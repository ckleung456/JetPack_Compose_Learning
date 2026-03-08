package com.example.feature.doordash.usecase

import com.example.core.usecase.FlowUseCase
import com.example.feature.doordash.model.domain.LikedStatus
import com.example.feature.doordash.model.domain.usecase.LikeRestaurantUseCaseInput
import com.example.feature.doordash.module.domain.DoordashDbRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ViewModelScoped
class LikeRestaurantUseCase @Inject constructor(
    val localRepository: DoordashDbRepository
) : FlowUseCase<LikeRestaurantUseCaseInput, LikedStatus>() {
    override suspend fun flowWork(input: LikeRestaurantUseCaseInput): Flow<LikedStatus> =
        flow {
            // TODO: In reality, it should be api call then
            // TODO: remove liked data from db if succeed and exist in db
            // TODO: save to db if failed
            // TODO: set a scheduler/job to retry in background if db has data
            val likedStatus = when(input.status) {
                LikedStatus.LIKED -> LikedStatus.UN_LIKED
                LikedStatus.UN_LIKED -> LikedStatus.LIKED
                else -> LikedStatus.NO_REF
            }
            localRepository.saveLikedStatus(
                restaurantId = input.restaurantId,
                likedStatus = likedStatus
            )
            likedStatus
        }
}