package com.example.domain.usecase

import com.example.domain.Error

sealed class UseCaseOutputWithStatus<out RESULT> {
    data class Progress<out RESULT>(val data: Any? = null) : UseCaseOutputWithStatus<RESULT>()
    data class Success<out RESULT>(val result: RESULT) : UseCaseOutputWithStatus<RESULT>()
    data class Failed<out RESULT>(
        val error: Error,
        val failedResult: RESULT? = null
    ) : UseCaseOutputWithStatus<RESULT>()
}