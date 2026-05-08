package com.deviantart.artviewer.di

import android.content.Context
import com.deviantart.artviewer.data.local.datastore.AuthenticationDataStore
import com.deviantart.artviewer.data.remote.LoginApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton


/**
 * Makes classes available to hilt so they can be injected when needed.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthDataStore(
        @ApplicationContext context: Context
    ): AuthenticationDataStore = AuthenticationDataStore(context)


    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()


    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.deviantart.com/")
            .client(client)
            .addConverterFactory(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }.asConverterFactory("application/json".toMediaType())
            )
            .build()


    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi =
        retrofit.create(LoginApi::class.java)
}