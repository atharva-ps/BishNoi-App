package com.app.bishnoi.presentation.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.toDomain
import com.app.bishnoi.domain.model.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

    init {
        loadMembers()
        loadStates()
    }

    fun loadMembers(
        search: String = _uiState.value.searchQuery,
        state: String = _uiState.value.selectedState
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val response = apiService.getAllMembers(
                    search = search,
                    state = state
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!
                    val members = data.members.map { it.toDomain() }

                    _uiState.update {
                        it.copy(
                            members = members,
                            totalCount = data.totalCount,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load members"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun loadStates() {
        viewModelScope.launch {
            try {
                val response = apiService.getAllStates()
                if (response.isSuccessful && response.body()?.success == true) {
                    val states = listOf("All States") + response.body()!!.states
                    _uiState.update { it.copy(availableStates = states) }
                }
            } catch (e: Exception) {
                // Silent fail - not critical
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadMembers(search = query)
    }

    fun onStateSelected(state: String) {
        _uiState.update { it.copy(selectedState = state) }
        loadMembers(state = state)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class MembersUiState(
    val members: List<Member> = emptyList(),
    val totalCount: Int = 0,
    val searchQuery: String = "",
    val selectedState: String = "All States",
    val availableStates: List<String> = listOf("All States"),
    val isLoading: Boolean = false,
    val error: String? = null
)
