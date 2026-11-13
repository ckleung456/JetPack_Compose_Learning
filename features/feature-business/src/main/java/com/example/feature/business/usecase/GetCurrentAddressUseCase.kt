package com.example.feature.business.usecase

import com.example.domain.ErrorExcept
import com.example.domain.Result
import com.example.domain.location.LocationAddressProvider
import com.example.shared.lib.FlowUseCase
import com.example.domain.usecase.UseCaseOutputWithStatus
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@ViewModelScoped
class GetCurrentAddressUseCase @Inject constructor(
    private val locationAddressProvider: LocationAddressProvider
): FlowUseCase<Unit, String, String>() {
    override suspend fun flowWork(input: Unit): Flow<String> = flow {
        when(val result = locationAddressProvider.getCurrentAddress()) {
            is Result.Success -> emit(result.data)
            is Result.Error -> throw ErrorExcept(error = result.error)
        }
    }

    override suspend fun onSucceedDataHandling(intermediate: String): UseCaseOutputWithStatus.Success<String> =
        UseCaseOutputWithStatus.Success(intermediate.trim())
}