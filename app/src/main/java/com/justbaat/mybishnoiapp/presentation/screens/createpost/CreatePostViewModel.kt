package com.justbaat.mybishnoiapp.presentation.screens.createpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.justbaat.mybishnoiapp.data.remote.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun setImageFile(file: File?) {
        _uiState.update { it.copy(selectedImage = file) }
    }

    fun setCaption(text: String) {
        _uiState.update { it.copy(caption = text) }
    }

    fun setVisibility(visibility: PostVisibility) {
        _uiState.update { it.copy(visibility = visibility) }
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
                val imageRequestBody =
                    imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    name = "image",
                    filename = imageFile.name,
                    body = imageRequestBody
                )

                val captionBody: RequestBody =
                    (state.caption.trim()).toRequestBody("text/plain".toMediaTypeOrNull())
                val visibilityBody: RequestBody =
                    state.visibility.value.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.createPost(
                    image = imagePart,
                    caption = captionBody,
                    visibility = visibilityBody
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            selectedImage = null,
                            caption = "",
                            visibility = PostVisibility.FOLLOWERS,
                            successMessage = "Post created successfully"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isUploading = false,
                            error = response.body()?.message ?: "Failed to create post"
                        )
                    }
                }
            } catch (e: Exception) {
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

data class CreatePostUiState(
    val selectedImage: File? = null,
    val caption: String = "",
    val visibility: PostVisibility = PostVisibility.FOLLOWERS,
    val isUploading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
