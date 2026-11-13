package com.example.feature.business.module.network

import com.example.feature.business.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(HEADER_AUTH, "Bearer ${BuildConfig.YELP_API_TOKEN}")
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val HEADER_AUTH = "Authorization"
    }
}