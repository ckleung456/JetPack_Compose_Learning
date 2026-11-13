package com.example.feature.business.usecase

import com.example.domain.usecase.UseCaseOutputWithStatus
import com.example.feature.business.model.domain.BusinessDetail
import com.example.feature.business.model.domain.BusinessDetailUi
import com.example.feature.business.model.domain.toBusinessDetailUi
import com.example.feature.business.module.network.BusinessRepository
import com.example.shared.lib.FlowUseCase
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GetBusinessDetailUseCase @Inject constructor(
    private val businessRepository: BusinessRepository
): FlowUseCase<String, BusinessDetail, BusinessDetailUi>() {
    override suspend fun flowWork(input: String): Flow<BusinessDetail> =
        businessRepository.getBusinessDetail(id = input)

    override suspend fun onSucceedDataHandling(intermediate: BusinessDetail): UseCaseOutputWithStatus.Success<BusinessDetailUi> =
        UseCaseOutputWithStatus.Success(
            result = intermediate.toBusinessDetailUi()
        )
}