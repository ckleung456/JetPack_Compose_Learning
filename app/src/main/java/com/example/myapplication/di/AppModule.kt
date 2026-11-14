package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.feature.doordash.module.domain.RestaurantLikedDao
import com.example.myapplication.module.local.MyAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    private const val DB_NAME = "MY_APP_DB"

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext context: Context
    ): MyAppDatabase = Room.databaseBuilder(context, MyAppDatabase::class.java, DB_NAME)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun provideRestaurantLikedDao(
        database: MyAppDatabase
    ): RestaurantLikedDao = database.restaurantLikedDao()
}
