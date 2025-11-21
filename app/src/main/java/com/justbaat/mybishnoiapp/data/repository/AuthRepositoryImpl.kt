package com.justbaat.mybishnoiapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.justbaat.mybishnoiapp.data.remote.api.ApiService
import com.justbaat.mybishnoiapp.data.remote.dto.GetEmailRequest
import com.justbaat.mybishnoiapp.data.remote.dto.LoginRequest
import com.justbaat.mybishnoiapp.data.remote.dto.RegisterRequest
import com.justbaat.mybishnoiapp.data.remote.dto.toDomainModel
import com.justbaat.mybishnoiapp.domain.model.User
import com.justbaat.mybishnoiapp.domain.repository.AuthRepository
import com.justbaat.mybishnoiapp.utils.Resource
import com.justbaat.mybishnoiapp.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val firebaseAuth: FirebaseAuth,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            // Step 1: Create user in Firebase
            val authResult = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("Firebase user creation failed")

            // Step 2: Register user in backend
            val registerRequest = RegisterRequest(
                username = name, // ✅ 'name' param is now used as username
                email = email,
                id = firebaseUser.uid
            )

            val response = apiService.register(registerRequest)

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user.toDomainModel()

                // Step 3: Save user data locally
                tokenManager.saveUserData(
                    userId = user.id,
                    email = user.email,
                    name = user.name
                )

                emit(Resource.Success(user))
            } else {
                // If backend registration fails, delete Firebase user
                firebaseUser.delete().await()
                emit(Resource.Error(response.message() ?: "Registration failed"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred during registration"))
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            // ✅ Check if input is email or username
            val loginEmail = if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // It's an email
                email
            } else {
                // It's a username - fetch email from backend
                val emailResponse = apiService.getEmailByUsername(GetEmailRequest(email))
                if (emailResponse.isSuccessful && emailResponse.body()?.success == true) {
                    emailResponse.body()!!.email!!
                } else {
                    throw Exception("Username not found")
                }
            }

            // Step 1: Sign in with Firebase using email
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(loginEmail, password)
                .await()

            val firebaseUser = authResult.user
                ?: throw Exception("Firebase sign in failed")

            // Step 2: Authenticate with backend
            val loginRequest = LoginRequest(
                identifier = email, // Original input (username or email)
                id = firebaseUser.uid
            )

            val response = apiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.user.toDomainModel()

                // Step 3: Save user data locally
                tokenManager.saveUserData(
                    userId = user.id,
                    email = user.email,
                    name = user.name
                )

                emit(Resource.Success(user))
            } else {
                emit(Resource.Error(response.message() ?: "Login failed"))
            }

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An error occurred during login"))
        }
    }


    override suspend fun logout(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            apiService.logout()
            firebaseAuth.signOut()
            tokenManager.clearUserData()

            emit(Resource.Success(Unit))

        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Logout failed"))
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    override fun getCurrentUser(): User? {
        val userId = tokenManager.getUserId()
        val email = tokenManager.getUserEmail()
        val name = tokenManager.getUserName()

        return if (userId != null && email != null && name != null) {
            User(id = userId, email = email, name = name)
        } else {
            null
        }
    }
}
