package com.example.domain

interface Error

sealed interface Result<out SUCCESS, out E : Error> {
    data class Success<out SUCCESS>(val data: SUCCESS) : Result<SUCCESS, Nothing>
    data class Error<out E : com.example.domain.Error>(val error: E) : Result<Nothing, E>
}

typealias EmptyResult<E> = Result<Unit, E>
