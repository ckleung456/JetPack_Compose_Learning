package com.example.feature.business.model.di

import com.example.feature.business.module.network.BusinessApi
import com.example.feature.business.module.network.BusinessApiService
import com.example.feature.business.module.network.BusinessRepository
import com.example.feature.business.module.network.BusinessRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class BusinessModule {
    @Binds
    @Singleton
    abstract fun bindBusinessRepository(impl: BusinessRepositoryImp): BusinessRepository

    companion object {
        @Singleton
        @Provides
        fun provideBusinessApiService(
            service: BusinessApiService
        ): BusinessApi = service.provideService()
    }
}