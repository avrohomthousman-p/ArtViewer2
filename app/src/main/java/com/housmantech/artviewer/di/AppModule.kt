package com.housmantech.artviewer.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import com.housmantech.artviewer.data.local.datastore.AppStateDataStore
import com.housmantech.artviewer.data.local.datastore.AuthenticationDataStore
import com.housmantech.artviewer.data.local.room.AppDatabase
import com.housmantech.artviewer.data.local.room.FolderDao
import com.housmantech.artviewer.data.remote.FolderApi
import com.housmantech.artviewer.data.remote.LoginApi
import com.housmantech.artviewer.data.remote.MediaApi
import com.housmantech.artviewer.data.repository.TokenManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton


/**
 * Makes classes available to hilt so they can be injected when needed.
 */
@Suppress("unused")
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
    fun provideAppStateDataStore(
        @ApplicationContext context: Context
    ): AppStateDataStore = AppStateDataStore(context)



    @Provides
    @Singleton
    fun provideTokenManager(): TokenManager {
        return TokenManager()
    }



    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .apply {
                        if (request.url.host == "www.deviantart.com"){
                            if (!tokenManager.isTokenExpired()) {
                                val accessToken = tokenManager.getAccessToken()
                                this.header("Authorization", "Bearer $accessToken")
                            }
                            else {
                                Log.e("OkHttp", "No access token found")
                            }
                        }
                    }
                    .build()
                chain.proceed(newRequest)
            }
            .build()



    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        return Retrofit.Builder()
            .baseUrl("https://www.deviantart.com/")
            .client(client)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }



    @Provides
    @Singleton
    fun provideLoginApi(retrofit: Retrofit): LoginApi =
        retrofit.create(LoginApi::class.java)



    @Provides
    @Singleton
    fun provideMediaApi(retrofit: Retrofit): MediaApi =
        retrofit.create(MediaApi::class.java)



    @Provides
    @Singleton
    fun provideFolderApi(retrofit: Retrofit): FolderApi =
        retrofit.create(FolderApi::class.java)



    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        dbProvider: javax.inject.Provider<AppDatabase>
    ): AppDatabase {
        return Room.databaseBuilder(
            app, AppDatabase::class.java, "app.db"
        ).build()
    }



    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao {
        return db.folderDao()
    }
}
