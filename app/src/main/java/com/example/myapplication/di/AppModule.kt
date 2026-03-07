package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.feature.doordash.module.domain.LikedDao
import com.example.myapplication.module.local.MyAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppModule {
    companion object {
        private const val PREF_NAME = "CK_APP"
    }

    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    @DispatcherMain
    @Singleton
    @Provides
    fun provideDispatcherMain() : CoroutineDispatcher = Dispatchers.Main

    @DispatcherIO
    @Singleton
    @Provides
    fun provideDispatcherIO() : CoroutineDispatcher = Dispatchers.IO

    @DispatcherDefault
    @Singleton
    @Provides
    fun provideDispatcherDefault() : CoroutineDispatcher = Dispatchers.Default

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext context: Context
    ) : MyAppDatabase = Room.databaseBuilder(
        context = context,
        MyAppDatabase::class.java,
        "MY_APP_DB"
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideLikedDao(
        database: MyAppDatabase
    ): LikedDao = database.likedDao()
}