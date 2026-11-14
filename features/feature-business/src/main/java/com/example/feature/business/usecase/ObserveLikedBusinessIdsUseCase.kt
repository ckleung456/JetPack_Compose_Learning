package com.example.feature.business.usecase

import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.module.domain.BusinessLikedStore
import com.example.shared.lib.FlowUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class ObserveLikedBusinessIdsUseCase @Inject constructor(
    private val businessLikedStore: BusinessLikedStore
) : FlowUseCase<Unit, Set<String>, Set<String>>() {
    override suspend fun flowWork(input: Unit): Flow<Set<String>> = businessLikedStore.businessLikedIds

    override suspend fun onSucceedDataHandling(intermediate: Set<String>): UseCaseOutputWithStatus.Success<Set<String>> =
        UseCaseOutputWithStatus.Success(intermediate)
}
