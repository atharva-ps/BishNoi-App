package com.justbaat.mybishnoiapp.domain.repository

import com.justbaat.mybishnoiapp.domain.model.User
import com.justbaat.mybishnoiapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Flow<Resource<User>>
    suspend fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun logout(): Flow<Resource<Unit>>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUser(): User?
}
