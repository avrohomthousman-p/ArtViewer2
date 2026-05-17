package com.deviantart.artviewer.di

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deviantart.artviewer.data.local.datastore.AuthenticationDataStore
import com.deviantart.artviewer.data.local.room.AppDatabase
import com.deviantart.artviewer.data.local.room.FolderDao
import com.deviantart.artviewer.data.remote.FolderApi
import com.deviantart.artviewer.data.remote.LoginApi
import com.deviantart.artviewer.data.remote.MediaApi
import com.deviantart.artviewer.data.repository.TokenManager
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                val newRequest = chain.request().newBuilder()
                    .apply {
                        if (!tokenManager.isTokenExpired()) {
                            val accessToken = tokenManager.getAccessToken()
                            this.header("Authorization", "Bearer $accessToken")
                        }
                        else {
                            Log.e("OkHttp", "No access token found")
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
        app: Application
    ): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app.db"
        ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // TODO: insert default folders here
                    //This is going to require a more complicated system
                    //For the moment here is some test only data
                    CoroutineScope(Dispatchers.IO).launch {
                        db.execSQL("""
                            INSERT INTO folders (
                                localId, remoteId, ownerUsername, storedIn, displayName, 
                                shouldRandomize, thumbnailUrl, totalImages
                            ) VALUES (
                                1, '97550DB4-84C9-EBE5-C389-DBEABE29DEB1', 'ulebulem', 
                                'GALLERY', 'Sample Folder A', 1, 
                                'https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/fee675fd-befb-4b14-95a6-d1417e56046a/djhp60z-ec3a11e9-ce53-4c2c-9e4b-085c601ac566.jpg/v1/fit/w_150,h_150,q_70,strp/_tree_in_the_morning_by_ulebulem_djhp60z-150.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9NzMyIiwicGF0aCI6Ii9mL2ZlZTY3NWZkLWJlZmItNGIxNC05NWE2LWQxNDE3ZTU2MDQ2YS9kamhwNjB6LWVjM2ExMWU5LWNlNTMtNGMyYy05ZTRiLTA4NWM2MDFhYzU2Ni5qcGciLCJ3aWR0aCI6Ijw9MTI4MCJ9XV0sImF1ZCI6WyJ1cm46c2VydmljZTppbWFnZS5vcGVyYXRpb25zIl19.Di7cx-KBq1kAmbnPss2GRUVtmC_V2xskIMQOsq0a04g',
                                 159
                            )
                        """.trimIndent())
                    }
                }
            }
        ).build()
    }



    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao {
        return db.folderDao()
    }
}