package com.example.feature.business.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

object Utils {
    fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
        .map {
            Result.success(it)
        }
        .catch {
            emit(Result.failure(it))
        }
}