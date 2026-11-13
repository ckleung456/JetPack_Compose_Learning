package com.example.location.di

import android.content.Context
import com.example.domain.location.LocationAddressProvider
import com.example.location.FusedLocationAddressProvider
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.shared.lib.di.DispatcherIO
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideLocationAddressProvider(
        @ApplicationContext context: Context,
        @DispatcherIO ioDispatcher: CoroutineDispatcher
    ): LocationAddressProvider =
        FusedLocationAddressProvider(
            context = context,
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context),
            ioDispatcher = ioDispatcher
        )
}