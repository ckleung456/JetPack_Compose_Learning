package com.example.feature.country.module.network

import com.example.core.network.FlowErrorHandlingCallAdapterFactory
import com.example.feature.country.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryApiService @Inject constructor(
    private val adapterFactory: FlowErrorHandlingCallAdapterFactory
) {
    fun provideService(): CountryApis = Retrofit.Builder()
        .client(
            OkHttpClient.Builder().apply {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }
                .build()
        )
        .baseUrl(BuildConfig.COUNTRY_SERVER_ENDPOINT)
        .addCallAdapterFactory(adapterFactory)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setStrictness(Strictness.LENIENT).create()))
        .build()
        .create(CountryApis::class.java)}