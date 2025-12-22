package com.app.bishnoi.di

import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.api.WordpressApiService
import com.app.bishnoi.data.repository.NewsRepositoryImpl
import com.app.bishnoi.domain.repository.NewsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
