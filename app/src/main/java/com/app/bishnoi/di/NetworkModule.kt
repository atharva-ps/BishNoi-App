package com.app.bishnoi.di

import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.api.WordpressApiService
import com.app.bishnoi.utils.Constants
import com.app.bishnoi.utils.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        tokenManager: TokenManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val originalRequest = chain.request()

                // Get Firebase bearer token
                val token = runBlocking { tokenManager.getToken() }

                val requestBuilder = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")

                // Add Authorization header if token exists
                token?.let {
                    requestBuilder.addHeader("Authorization", "Bearer $it")
                }

                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @BishnoiRetrofit
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Main BishNoi ApiService
    @Provides
    @Singleton
    fun provideApiService(@BishnoiRetrofit retrofit: Retrofit): ApiService {  // ✅ Use qualifier
        return retrofit.create(ApiService::class.java)
    }

    // 3) WordPress Retrofit – NO auth header, simple client
    @Provides
    @Singleton
    @WordpressRetrofit
    fun provideWordpressRetrofit(
        loggingInterceptor: HttpLoggingInterceptor
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://news.bishnoiapp.com/")   // your WordPress base URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // WordPress ApiService
    @Provides
    @Singleton
    fun provideWordpressApiService(
        @WordpressRetrofit wordpressRetrofit: Retrofit  // ✅ Use qualifier
    ): WordpressApiService {
        return wordpressRetrofit.create(WordpressApiService::class.java)
    }
}
