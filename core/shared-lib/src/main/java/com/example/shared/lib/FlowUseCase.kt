package com.example.shared.lib

import com.example.domain.Error
import com.example.domain.ErrorExcept
import com.example.domain.UnexpectedError
import com.example.domain.usecase.UseCaseOutputWithStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

abstract class FlowUseCase<in INPUT, INTERMEDIATE, out RESULT> {

    protected abstract suspend fun flowWork(
        input: INPUT
    ): Flow<INTERMEDIATE>

    protected open suspend fun errorResult(error: Throwable): RESULT? = null

    open fun isValidInput(input: INPUT) : Boolean = true

    abstract suspend fun onSucceedDataHandling(
        intermediate: @UnsafeVariance INTERMEDIATE
    ) : UseCaseOutputWithStatus.Success<RESULT>

    operator fun invoke(
        input: INPUT
    ): Flow<UseCaseOutputWithStatus<RESULT>> = flow {
        try {
            if (!isValidInput(input)) {
                throw IllegalArgumentException()
            }
            emit(UseCaseOutputWithStatus.Progress())
            flowWork(input = input).map {
                onSucceedDataHandling(intermediate = it)
            }.collect {
                emit(it)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val error = when (e) {
                is ErrorExcept -> e.error // a domain Error carried through an exception (e.g. LocationError)
                is Error -> e             // exceptions that are domain errors themselves (e.g. RetrofitException)
                else -> UnexpectedError(message = e.message?.ifBlank { null } ?: "Unknown Error")
            }
            emit(
                UseCaseOutputWithStatus.Failed(
                    error = error,
                    failedResult = errorResult(error = e)
                )
            )
        }
    }
}
