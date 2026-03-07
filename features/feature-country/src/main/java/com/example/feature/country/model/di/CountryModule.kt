package com.example.feature.country.model.di

import com.example.feature.country.module.network.CountryApiService
import com.example.feature.country.module.network.CountryApis
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class CountryModule {

    @Singleton
    @Provides
    fun provideCountryApiService(
        service: CountryApiService
    ): CountryApis = service.provideService()

}