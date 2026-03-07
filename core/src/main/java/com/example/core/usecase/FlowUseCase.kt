package com.example.core.usecase

import com.example.core.network.RetrofitException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart

abstract class FlowUseCase<in INPUT, out RESULT> {

    protected abstract suspend fun flowWork(
        input: INPUT
    ): Flow<RESULT>

    protected open suspend fun errorResult(error: Throwable): RESULT? = null

    open fun isValidInput(input: INPUT) : Boolean = true

    open fun onSucceedDataHandling(
        result: @UnsafeVariance RESULT,
        onResultFn: (UseCaseOutputWithStatus<RESULT>) -> Unit
    ) {
        onResultFn.invoke(UseCaseOutputWithStatus.Success(result = result))
    }

    suspend operator fun invoke(
        input: INPUT,
        onResultFn: (UseCaseOutputWithStatus<RESULT>) -> Unit
    ) = flowWork(input = input)
        .onStart {
            onResultFn.invoke(UseCaseOutputWithStatus.Progress())
            if (!isValidInput(input)) {
                throw IllegalArgumentException()
            }
        }
        .flowOn(Dispatchers.IO)
        .onEach { result ->
            onSucceedDataHandling(result = result, onResultFn = onResultFn)
        }
        .catch { e ->
            onResultFn.invoke(
                UseCaseOutputWithStatus.Failed(
                    error = e as? RetrofitException
                        ?: RetrofitException.UnexpectedError(
                            exception = e
                        ),
                    failedResult = errorResult(error = e)
                )
            )
        }
        .flowOn(Dispatchers.Main)
        .collect()
}