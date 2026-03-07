package com.example.core.usecase

import com.example.core.network.RetrofitException

sealed class UseCaseOutputWithStatus<out RESULT> {
    data class Progress<out RESULT>(val data: Any? = null) : UseCaseOutputWithStatus<RESULT>()
    data class Success<out RESULT>(val result: RESULT) : UseCaseOutputWithStatus<RESULT>()
    data class Failed<out RESULT>(
        val error: RetrofitException,
        val failedResult: RESULT? = null
    ) : UseCaseOutputWithStatus<RESULT>()
}