package com.justbaat.mybishnoiapp.di

import com.justbaat.mybishnoiapp.data.repository.AuthRepositoryImpl
import com.justbaat.mybishnoiapp.data.repository.ProfileRepositoryImpl
import com.justbaat.mybishnoiapp.domain.repository.AuthRepository
import com.justbaat.mybishnoiapp.domain.repository.ProfileRepository
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
}
