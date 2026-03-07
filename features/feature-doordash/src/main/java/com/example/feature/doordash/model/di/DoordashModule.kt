package com.example.feature.doordash.model.di

import com.example.feature.doordash.module.network.DoorDashAPIService
import com.example.feature.doordash.module.network.DoorDashAPIs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DoordashModule {

    @Singleton
    @Provides
    fun provideDoordashService(
        service: DoorDashAPIService
    ): DoorDashAPIs = service.provideService()

}