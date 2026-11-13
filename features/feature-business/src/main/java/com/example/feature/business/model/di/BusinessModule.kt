package com.example.feature.business.model.di

import com.example.feature.business.BuildConfig
import com.example.feature.business.module.network.AuthInterceptor
import com.example.feature.business.module.network.BusinessApi
import com.example.feature.business.module.network.BusinessRepository
import com.example.feature.business.module.network.BusinessRepositoryImp
import com.example.network.FlowErrorHandlingCallAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
        fun provideBusinessApi(
            authInterceptor: AuthInterceptor,
            adapterFactory: FlowErrorHandlingCallAdapterFactory
        ): BusinessApi {
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    }
                }
                .build()
            return Retrofit.Builder()
                .client(client)
                .baseUrl(BuildConfig.YELP_SERVER_ENDPOINT)
                .addCallAdapterFactory(adapterFactory)
                // Strict parsing (no LENIENT) so malformed responses surface as errors instead of half-parsed data.
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BusinessApi::class.java)
        }
    }
}
