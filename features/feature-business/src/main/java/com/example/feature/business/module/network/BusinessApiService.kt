package com.example.feature.business.module.network

import com.example.network.FlowErrorHandlingCallAdapterFactory
import com.example.feature.business.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BusinessApiService @Inject constructor(
    private val adapterFactory: FlowErrorHandlingCallAdapterFactory
){
    fun provideService(): BusinessApi = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
                .build()
        )
        .baseUrl(BuildConfig.YELP_SERVER_ENDPOINT)
        .addCallAdapterFactory(adapterFactory)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setStrictness(Strictness.LENIENT).create()))
        .build()
        .create(BusinessApi::class.java)
}