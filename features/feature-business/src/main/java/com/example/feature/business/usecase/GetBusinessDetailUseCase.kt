package com.example.feature.business.usecase

import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.model.domain.BusinessDetail
import com.example.feature.business.model.domain.BusinessDetailUi
import com.example.feature.business.model.domain.toBusinessDetailUi
import com.example.feature.business.module.domain.BusinessLikedStore
import com.example.feature.business.module.network.BusinessRepository
import com.example.shared.lib.FlowUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class GetBusinessDetailUseCase @Inject constructor(
    private val businessRepository: BusinessRepository,
    private val businessLikedStore: BusinessLikedStore
): FlowUseCase<String, Pair<BusinessDetail, Boolean>, BusinessDetailUi>() {
    override suspend fun flowWork(input: String): Flow<Pair<BusinessDetail, Boolean>> =
        combine(
            businessRepository.getBusinessDetail(id = input),
            businessLikedStore.businessLikedIds.map { input in it }.distinctUntilChanged()
        ) { detail, liked -> detail to liked }

    override suspend fun onSucceedDataHandling(intermediate: Pair<BusinessDetail, Boolean>): UseCaseOutputWithStatus.Success<BusinessDetailUi> =
        UseCaseOutputWithStatus.Success(
            result = intermediate.first.toBusinessDetailUi(liked = intermediate.second)
        )
}
