package com.example.feature.business.usecase

import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.module.domain.BusinessLikedStore
import com.example.shared.lib.FlowUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Toggles the like status of a business. Emits the new liked state once and completes.
 * Screens get like changes from [ObserveLikedBusinessIdsUseCase] / [GetBusinessDetailUseCase], not from this.
 */
@ViewModelScoped
class ToggleBusinessLikeUseCase @Inject constructor(
    private val businessLikedStore: BusinessLikedStore
) : FlowUseCase<String, Boolean, Boolean>() {
    override suspend fun flowWork(input: String): Flow<Boolean> = flow {
        emit(businessLikedStore.toggleBusinessLiked(businessId = input))
    }

    override suspend fun onSucceedDataHandling(intermediate: Boolean): UseCaseOutputWithStatus.Success<Boolean> =
        UseCaseOutputWithStatus.Success(intermediate)
}
