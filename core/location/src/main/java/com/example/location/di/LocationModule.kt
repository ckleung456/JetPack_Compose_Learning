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
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideLocationAddressProvider(@ApplicationContext context: Context): LocationAddressProvider =
        FusedLocationAddressProvider(
            context = context,
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context),
            ioDispatcher = Dispatchers.IO
        )
}