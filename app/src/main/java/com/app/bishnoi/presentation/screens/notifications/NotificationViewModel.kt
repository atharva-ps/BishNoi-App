package com.app.bishnoi.presentation.screens.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bishnoi.data.remote.api.ApiService
import com.app.bishnoi.data.remote.dto.toDomain
import com.app.bishnoi.domain.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "NotificationVM"
    }

    fun loadNotifications() {
        viewModelScope.launch {
            Log.d(TAG, "📥 Starting to load notifications...")
            _uiState.update { it.copy(isLoading = true) }

            try {
                val response = apiService.getNotifications(limit = 50)

                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Notifications count: ${body?.notifications?.size ?: 0}")

                    if (body?.success == true) {
                        val notifications = body.notifications.map { dto ->
                            dto.toDomain()
                        }

                        Log.d(TAG, "✅ Loaded ${notifications.size} notifications")

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                notifications = notifications,
                                error = null
                            )
                        }
                    } else {
                        Log.e(TAG, "❌ Response success=false: ${body?.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = body?.message ?: "Failed to load notifications"
                            )
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "❌ Response not successful: $errorBody")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load notifications"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading notifications: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📖 Marking notification as read: $notificationId")
                val response = apiService.markNotificationAsRead(notificationId)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Notification marked as read")
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { notification ->
                                if (notification.id == notificationId) {
                                    notification.copy(isRead = true)
                                } else notification
                            }
                        )
                    }
                } else {
                    Log.e(TAG, "❌ Failed to mark as read: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error marking as read: ${e.message}")
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📖 Marking all notifications as read")
                val response = apiService.markAllNotificationsAsRead()

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ All notifications marked as read")
                    _uiState.update { state ->
                        state.copy(
                            notifications = state.notifications.map { it.copy(isRead = true) }
                        )
                    }
                } else {
                    Log.e(TAG, "❌ Failed to mark all as read: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error marking all as read: ${e.message}")
            }
        }
    }

    // ✅ NEW: Delete notification
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🗑️ Deleting notification: $notificationId")

                // Optimistically remove from UI
                _uiState.update { state ->
                    state.copy(
                        notifications = state.notifications.filter { it.id != notificationId }
                    )
                }

                val response = apiService.deleteNotification(notificationId)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Notification deleted successfully")
                } else {
                    Log.e(TAG, "❌ Failed to delete notification: ${response.message()}")
                    // Reload notifications if delete failed
                    loadNotifications()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error deleting notification: ${e.message}")
                // Reload notifications if delete failed
                loadNotifications()
            }
        }
    }

    fun getUnreadCount() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "📊 Getting unread count")
                val response = apiService.getUnreadNotificationCount()

                if (response.isSuccessful && response.body()?.success == true) {
                    val count = response.body()!!.count
                    Log.d(TAG, "✅ Unread count: $count")
                    _uiState.update {
                        it.copy(unreadCount = count)
                    }
                } else {
                    Log.e(TAG, "❌ Failed to get unread count: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error getting unread count: ${e.message}")
            }
        }
    }
}

data class NotificationUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: String? = null
)
