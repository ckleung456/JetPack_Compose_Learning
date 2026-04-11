package com.example.core.usecase

import com.example.core.network.RetrofitException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

abstract class FlowUseCase<in INPUT, INTERMEDIATE, out RESULT> {

    protected abstract suspend fun flowWork(
        input: INPUT
    ): Flow<INTERMEDIATE>

    protected open suspend fun errorResult(error: Throwable): RESULT? = null

    open fun isValidInput(input: INPUT) : Boolean = true

    abstract suspend fun onSucceedDataHandling(
        intermediate: @UnsafeVariance INTERMEDIATE
    ) : UseCaseOutputWithStatus.Success<RESULT>

    fun invoke(
        input: INPUT
    ): Flow<UseCaseOutputWithStatus<RESULT>> = flow {
        if (!isValidInput(input)) {
            throw IllegalArgumentException()
        }
        emit(UseCaseOutputWithStatus.Progress())
        try {
            flowWork(input = input).map {
                onSucceedDataHandling(intermediate = it)
            }.collect {
                emit(it)
            }
        } catch (e: Exception) {
            emit(
                UseCaseOutputWithStatus.Failed(
                    error = e as? RetrofitException
                        ?: RetrofitException.UnexpectedError(
                            exception = e
                        ),
                    failedResult = errorResult(error = e)
                )
            )
        }
    }
}