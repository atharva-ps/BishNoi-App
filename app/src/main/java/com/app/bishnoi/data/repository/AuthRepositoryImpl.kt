package com.app.bishnoi.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.GetEmailRequest
import com.app.bishnoi.data.remote.dto.LoginRequest
import com.app.bishnoi.data.remote.dto.RegisterRequest
import com.app.bishnoi.data.remote.dto.toDomainModel
import com.app.bishnoi.domain.model.User
import com.app.bishnoi.domain.repository.AuthRepository
import com.app.bishnoi.utils.Resource
import com.app.bishnoi.utils.TokenManager
import com.app.bishnoi.utils.parseErrorMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import java.io.IOException
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

            try {
                // Step 2: Register user in backend
                val registerRequest = RegisterRequest(
                    username = name,
                    email = email,
                    id = firebaseUser.uid
                )

                val response = apiService.register(registerRequest)

                if (response.isSuccessful && response.body() != null) {
                    val responseBody = response.body()!!

                    if (responseBody.success && responseBody.user != null) {
                        val user = responseBody.user.toDomainModel()

                        // Step 3: Save user data locally
                        tokenManager.saveUserData(
                            userId = user.id,
                            email = user.email,
                            name = user.name
                        )

                        emit(Resource.Success(user))
                    } else {
                        // Backend returned success=false
                        firebaseUser.delete().await()
                        val errorMsg = responseBody.message ?: "Registration failed"
                        emit(Resource.Error(errorMsg))
                    }
                } else {
                    // ✅ Parse error from response body
                    firebaseUser.delete().await()

                    val errorMessage = try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody != null) {
                            val gson = com.google.gson.Gson()
                            val errorResponse = gson.fromJson(
                                errorBody,
                                com.app.bishnoi.data.remote.dto.ErrorResponse::class.java
                            )
                            errorResponse.message.ifBlank { "Registration failed" }
                        } else {
                            "Registration failed"
                        }
                    } catch (e: Exception) {
                        "Registration failed"
                    }

                    emit(Resource.Error(errorMessage))
                }
            } catch (e: Exception) {
                // If backend registration fails, delete Firebase user
                try {
                    firebaseUser.delete().await()
                } catch (deleteError: Exception) {
                    // Ignore deletion error
                }
                throw e
            }

        } catch (e: HttpException) {
            // ✅ Parse error message from HTTP exception
            val errorMessage = e.parseErrorMessage()
            emit(Resource.Error(errorMessage))

        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection."))

        } catch (e: Exception) {
            // Handle Firebase Auth errors and other exceptions
            val errorMessage = when {
                e.message?.contains("email address is already in use", ignoreCase = true) == true ->
                    "This email is already registered"
                e.message?.contains("password", ignoreCase = true) == true ->
                    "Password should be at least 6 characters"
                e.message?.contains("email", ignoreCase = true) == true ->
                    "Please enter a valid email address"
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error. Please check your connection."
                else -> e.message ?: "An error occurred during registration"
            }
            emit(Resource.Error(errorMessage))
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            // Check if input is email or username
            val loginEmail = if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
                identifier = email,
                id = firebaseUser.uid
            )

            val response = apiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!

                if (responseBody.success && responseBody.user != null) {
                    val user = responseBody.user.toDomainModel()

                    // Step 3: Save user data locally
                    tokenManager.saveUserData(
                        userId = user.id,
                        email = user.email,
                        name = user.name
                    )

                    emit(Resource.Success(user))
                } else {
                    val errorMsg = responseBody.message ?: "Login failed"
                    emit(Resource.Error(errorMsg))
                }
            } else {
                // ✅ Parse error from response body
                val errorMessage = try {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        val gson = com.google.gson.Gson()
                        val errorResponse = gson.fromJson(
                            errorBody,
                            com.app.bishnoi.data.remote.dto.ErrorResponse::class.java
                        )
                        errorResponse.message.ifBlank { "Login failed" }
                    } else {
                        "Login failed"
                    }
                } catch (e: Exception) {
                    "Login failed"
                }

                emit(Resource.Error(errorMessage))
            }

        } catch (e: HttpException) {
            val errorMessage = e.parseErrorMessage()
            emit(Resource.Error(errorMessage))

        } catch (e: IOException) {
            emit(Resource.Error("Network error. Please check your internet connection."))

        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("password is invalid", ignoreCase = true) == true ->
                    "Incorrect password"
                e.message?.contains("no user record", ignoreCase = true) == true ->
                    "No account found with this email"
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Network error. Please check your connection."
                else -> e.message ?: "An error occurred during login"
            }
            emit(Resource.Error(errorMessage))
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

    override suspend fun sendPasswordReset(emailOrUsername: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())

            // Resolve username → email if needed
            val resolvedEmail = if (android.util.Patterns.EMAIL_ADDRESS
                    .matcher(emailOrUsername).matches()
            ) {
                emailOrUsername
            } else {
                val res = apiService.getEmailByUsername(GetEmailRequest(emailOrUsername))
                if (res.isSuccessful && res.body()?.success == true && res.body()!!.email != null) {
                    res.body()!!.email!!
                } else {
                    throw Exception("No account found with this email/username")
                }
            }

            firebaseAuth.sendPasswordResetEmail(resolvedEmail).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            val msg = when {
                e.message?.contains("no user record", ignoreCase = true) == true ->
                    "No account found with this email/username"
                e.message?.contains("badly formatted", ignoreCase = true) == true ->
                    "Please enter a valid email"
                else -> e.message ?: "Failed to send reset link"
            }
            emit(Resource.Error(msg))
        }
    }
}