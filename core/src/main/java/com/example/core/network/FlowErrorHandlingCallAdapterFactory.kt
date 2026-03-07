package com.example.core.network

import android.util.Log
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resumeWithException

@Singleton
class FlowErrorHandlingCallAdapterFactory @Inject constructor() : CallAdapter.Factory() {
    companion object {
        private const val ERROR_FLOW_RETURN_TYPE = "Invalid return type"
        private const val ERROR_FLOW_RESPONSE_TYPE = "Invalid response type"

        private const val ERROR_EMPTY_RESPONSE_BODY = "Response empty"

        private fun Call<*>.registerOnCancellation(continuation: CancellableContinuation<*>) {
            continuation.invokeOnCancellation {
                try {
                    cancel()
                } catch (ex: Exception) {
                    Log.e("FlowAdapter", ex.message, ex)
                }
            }
        }

        private fun <T> Call<T>.registerCallback(
            continuation: CancellableContinuation<*>,
            retrofit: Retrofit,
            type: Type,
            success: (response: Response<T>) -> Unit
        ) {
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    success.invoke(response)
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    when (throwable) {
                        is HttpException -> continuation.resumeWithException(
                            throwable.response()?.let { response ->
                                RetrofitException.HttpError(
                                    url = response.raw().request.url.toString(),
                                    response = response,
                                    retrofit = retrofit,
                                    successType = type
                                )
                            } ?: RetrofitException.UnexpectedError(throwable)
                        )
                        is ConnectException,
                        is SocketTimeoutException,
                        is UnknownHostException -> {
                            val ioException = IOException(
                                if (throwable is SocketTimeoutException) "Networt timeout" else "Network issue"
                            )
                            continuation.resumeWithException(
                                RetrofitException.NetworkError(ioException)
                            )
                        }
                        is IOException -> continuation.resumeWithException(
                            RetrofitException.NetworkError(throwable)
                        )
                        else -> continuation.resumeWithException(
                            RetrofitException.UnexpectedError(throwable)
                        )
                    }
                }
            })
        }
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation?>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (Flow::class.java != getRawType(returnType)) return null

        if (returnType !is ParameterizedType) {
            throw IllegalStateException(ERROR_FLOW_RETURN_TYPE)
        }

        val responseType = getParameterUpperBound(0, returnType)

        return when(getRawType(responseType)) {
            Response::class.java -> if (responseType !is ParameterizedType) {
                throw IllegalStateException(ERROR_FLOW_RESPONSE_TYPE)
            } else {
                FlowResponseCallAdapter<Any>(
                    retrofit = retrofit,
                    responseType = getParameterUpperBound(0, responseType)
                )
            }
            else -> FlowBodyCallAdapter<Any>(
                retrofit = retrofit,
                responseType = responseType
            )
        }
    }

    private class FlowResponseCallAdapter<T> (
        private val retrofit: Retrofit,
        private val responseType: Type
    ) : CallAdapter<T, Flow<Response<T>>> {
        override fun responseType(): Type = responseType

        override fun adapt(call: Call<T>): Flow<Response<T>> = flow {
            emit(suspendCancellableCoroutine { cancellableContinuation ->
                call.registerCallback(
                    continuation = cancellableContinuation,
                    retrofit = retrofit,
                    type = responseType
                ) { response ->
                    cancellableContinuation.resumeWith(runCatching {
                        if (response.isSuccessful) {
                            response
                        } else {
                            throw RetrofitException.HttpError(
                                url = response.raw().request.url.toString(),
                                response = response,
                                retrofit = retrofit,
                                successType = responseType
                            )
                        }
                    })
                }
                call.registerOnCancellation(continuation = cancellableContinuation)
            })
        }
    }

    private class FlowBodyCallAdapter<T>(
        private val retrofit: Retrofit,
        private val responseType: Type
    ) : CallAdapter<T, Flow<T>> {
        override fun responseType() = responseType

        @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.InternalCoroutinesApi::class)
        override fun adapt(call: Call<T>): Flow<T> = flow {
            emit(suspendCancellableCoroutine { cancellableContinuation ->
                call.registerCallback(
                    continuation = cancellableContinuation,
                    retrofit = retrofit,
                    type = responseType
                ) { response ->
                    cancellableContinuation.resumeWith(kotlin.runCatching {
                        if (response.isSuccessful) {
                            response.body()
                                ?: throw RetrofitException.UnexpectedError(IllegalStateException("$ERROR_EMPTY_RESPONSE_BODY $response"))
                        } else {
                            throw RetrofitException.HttpError(
                                url = response.raw().request.url.toString(),
                                response = response,
                                retrofit = retrofit,
                                successType = responseType
                            )
                        }
                    })
                }
                call.registerOnCancellation(continuation = cancellableContinuation)
            })
        }
    }
}