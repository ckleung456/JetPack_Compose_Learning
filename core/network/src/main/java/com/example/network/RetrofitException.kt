package com.example.network

import com.example.domain.DataError
import com.example.domain.Kind
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

sealed class RetrofitException constructor(
    open val errorMessage: String?,
    open val url: String,
    open val response: Response<*>?,
    open val successType: Type?,
    override val kind: Kind,
    open val exception: Throwable?,
    open val retrofit: Retrofit?
) : RuntimeException(errorMessage, exception), DataError {
    data class NetworkError(
        override val exception: IOException
    ) : RetrofitException(
        errorMessage = exception.message,
        url = "",
        response = null,
        successType = null,
        kind = Kind.NETWORK,
        exception = exception,
        retrofit = null
    )

    data class HttpError(
        override val url: String,
        override val response: Response<*>,
        override val retrofit: Retrofit,
        override val successType: Type
    ) : RetrofitException(
        errorMessage = "${response.code().toString()} - ${response.message()}",
        url = url,
        response = response,
        successType = successType,
        kind = Kind.HTTP,
        exception = null,
        retrofit = retrofit
    )

    data class UnexpectedError(
        override val exception: Throwable
    ) : RetrofitException(
        errorMessage = exception.message.orEmpty(),
        url = "",
        response = null,
        successType = null,
        kind = Kind.UNEXPECTED,
        exception = exception,
        retrofit = null
    )
}
