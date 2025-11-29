package com.notifa.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.notifa.ai.data.NotificationEntity
import com.notifa.ai.data.NotificationRepository
import com.notifa.ai.util.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class NotificationListViewModel(private val repository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _filter = MutableStateFlow<Priority?>(null)
    val filter: StateFlow<Priority?> = _filter.asStateFlow()

    init {
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            _filter.flatMapLatest { priority ->
                if (priority == null) {
                    repository.getAllNotifications()
                } else {
                    repository.getNotificationsByPriority(priority.name)
                }
            }.collect { notifications ->
                _notifications.value = notifications
            }
        }
    }

    fun setFilter(priority: Priority?) {
        _filter.value = priority
    }
}

class NotificationListViewModelFactory(private val repository: NotificationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
