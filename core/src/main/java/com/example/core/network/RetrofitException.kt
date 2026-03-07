package com.example.core.network

import com.google.gson.Gson
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type

sealed class RetrofitException constructor(
    open val errorMessage: String?,
    open val url: String,
    open val response: Response<*>?,
    open val successType: Type?,
    open val kind: Kind,
    open val exception: Throwable?,
    open val retrofit: Retrofit?,
    open val gson: Gson
) : RuntimeException(errorMessage, exception) {
    data class NetworkError(
        override val exception: IOException
    ) : RetrofitException(
        errorMessage = exception.message,
        url = "",
        response = null,
        successType = null,
        kind = Kind.NETWORK,
        exception = exception,
        retrofit = null,
        gson =Gson()
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
        retrofit = retrofit,
        gson = Gson()
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
        retrofit = null,
        gson = Gson()
    )
}

/**
 * Identifies the event mKind which triggered a [RetrofitException].
 */
enum class Kind {
    /**
     * An [IOException] occurred while communicating to the server.
     */
    NETWORK,

    /**
     * An exception was thrown while (de)serializing a body.
     */
    CONVERSION,

    /**
     * A non-200 HTTP status code was received from the server.
     */
    HTTP,

    /**
     * An internal error occurred while attempting to execute a request. It is best practice to
     * re-throw this exception so your application crashes.
     */
    UNEXPECTED
}