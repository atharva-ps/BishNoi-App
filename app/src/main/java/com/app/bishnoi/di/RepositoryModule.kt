package com.app.bishnoi.di

import com.app.bishnoi.data.repository.AuthRepositoryImpl
import com.app.bishnoi.data.repository.NewsRepositoryImpl
import com.app.bishnoi.data.repository.ProfileRepositoryImpl
import com.app.bishnoi.domain.repository.AuthRepository
import com.app.bishnoi.domain.repository.NewsRepository
import com.app.bishnoi.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNewsRepository(
        newsRepositoryImpl: NewsRepositoryImpl
    ): NewsRepository
}
