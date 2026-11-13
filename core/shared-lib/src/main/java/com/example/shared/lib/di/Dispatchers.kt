package com.example.shared.lib.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherMain

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherIO

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DispatcherDefault

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @DispatcherMain
    @Provides
    fun provideDispatcherMain(): CoroutineDispatcher = Dispatchers.Main

    @DispatcherIO
    @Provides
    fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO

    @DispatcherDefault
    @Provides
    fun provideDispatcherDefault(): CoroutineDispatcher = Dispatchers.Default
}
