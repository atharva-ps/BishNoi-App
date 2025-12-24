package com.app.bishnoi.presentation.screens.createpost

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import com.app.bishnoi.presentation.screens.createpost.PostFormat
import com.app.bishnoi.utils.TokenManager

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val httpClient = OkHttpClient()

    // ✅ ADD THIS INIT BLOCK
    init {
        viewModelScope.launch {
            val isAdmin = tokenManager.isAdmin()
            _uiState.update { it.copy(isAdmin = isAdmin) }
        }
    }

    fun setImageFile(file: File?) {
        _uiState.update { it.copy(selectedImage = file) }
    }

    fun setFormat(format: PostFormat) {
        _uiState.update { it.copy(format = format) }
    }


    fun setCaption(text: String) {
        _uiState.update { it.copy(caption = text) }
    }

    fun setVisibility(visibility: PostVisibility) {
        _uiState.update { it.copy(visibility = visibility) }
    }

    // ✅ ADD THIS METHOD
    fun setIsSocial(isSocial: Boolean) {
        _uiState.update { it.copy(isSocial = isSocial) }
    }

    fun createPost(onSuccess: () -> Unit) {
        val state = _uiState.value
        val imageFile = state.selectedImage

        if (imageFile == null) {
            _uiState.update { it.copy(error = "Please select an image") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null, successMessage = null) }

            try {
                Log.d("CreatePost", "Step 1: Getting presigned URL for ${imageFile.name}")

                // Step 1: Get presigned URL from backend
                val presignedResponse = apiService.getPresignedUrl(imageFile.name)

                if (!presignedResponse.isSuccessful || presignedResponse.body() == null) {
                    Log.e("CreatePost", "Failed to get presigned URL: ${presignedResponse.code()}")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = "Failed to get upload URL: ${presignedResponse.code()}"
                        )
                    }
                    return@launch
                }

                val presignedData = presignedResponse.body()!!
                val uploadUrl = presignedData.uploadUrl
                val imageUrl = presignedData.fileUrl

                Log.d("CreatePost", "Step 2: Uploading to S3")

                // Step 2: Upload image directly to S3 (ASYNC)
                val contentType = when (imageFile.extension.lowercase()) {
                    "png" -> "image/png"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    "heic", "heif" -> "image/heic"
                    else -> "image/jpeg"
                }

                val imageRequestBody = imageFile.asRequestBody(contentType.toMediaTypeOrNull())

                val s3Request = Request.Builder()
                    .url(uploadUrl)
                    .put(imageRequestBody)
                    .build()

                // ✅ Execute async call on IO dispatcher
                val s3Response = withContext(Dispatchers.IO) {
                    suspendCoroutine { continuation ->
                        httpClient.newCall(s3Request).enqueue(object : okhttp3.Callback {
                            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                                continuation.resumeWithException(e)
                            }

                            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                                continuation.resume(response)
                            }
                        })
                    }
                }

                if (!s3Response.isSuccessful) {
                    val errorBody = s3Response.body?.string() ?: "No error details"
                    Log.e("CreatePost", "S3 upload failed: ${s3Response.code} - $errorBody")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = "Failed to upload image: ${s3Response.code}"
                        )
                    }
                    return@launch
                }

                Log.d("CreatePost", "Step 3: Creating post with URL: $imageUrl")

                // Step 3: Create post with image URL
                val response = apiService.createPostWithUrl(
                    imageUrl = imageUrl,
                    caption = state.caption.trim(),
                    visibility = state.visibility.value,
                    format = state.format.name,
                    isSocial = state.isSocial
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("CreatePost", "Post created successfully!")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            selectedImage = null,
                            caption = "",
                            visibility = PostVisibility.FOLLOWERS,
                            isSocial = false,
                            successMessage = "Post created successfully"
                        )
                    }
                    onSuccess()
                } else {
                    Log.e("CreatePost", "Failed to create post: ${response.body()?.message}")
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = response.body()?.message ?: "Failed to create post"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("CreatePost", "Exception: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        error = e.message ?: "Unexpected error"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}

enum class PostVisibility(val value: String) {
    PUBLIC("public"),
    FOLLOWERS("followers")
}

enum class PostFormat {
    VERTICAL,
    HORIZONTAL
}

data class CreatePostUiState(
    val selectedImage: File? = null,
    val caption: String = "",
    val visibility: PostVisibility = PostVisibility.FOLLOWERS,
    val format: PostFormat = PostFormat.VERTICAL,
    val isSocial: Boolean = false,  // ✅ ADD THIS LINE
    val isAdmin: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
