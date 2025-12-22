package com.app.bishnoi.domain.repository

import com.app.bishnoi.domain.model.User
import com.app.bishnoi.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun register(name: String, email: String, password: String): Flow<Resource<User>>
    suspend fun login(email: String, password: String): Flow<Resource<User>>
    suspend fun logout(): Flow<Resource<Unit>>
    suspend fun sendPasswordReset(emailOrUsername: String): Flow<Resource<Unit>>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUser(): User?
}
