package com.notif.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notif.ai.data.NotificationEntity
import com.notif.ai.data.NotificationRepository
import com.notif.ai.util.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationListViewModel(private val repository: NotificationRepository) : ViewModel() {

    val myPriorityCount: StateFlow<Int> = repository.countByPriority(Priority.MY_PRIORITY.name)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)
    val importantCount: StateFlow<Int> = repository.countByPriority(Priority.IMPORTANT.name)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)
    val promotionalCount: StateFlow<Int> = repository.countByPriority(Priority.PROMOTIONAL.name)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)
    val spamCount: StateFlow<Int> = repository.countByPriority(Priority.SPAM.name)
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private val _notifications = MutableStateFlow<List<NotificationEntity>>(emptyList())
    val notifications: StateFlow<List<NotificationEntity>> = _notifications.asStateFlow()

    private val _filter = MutableStateFlow<Priority?>(null)

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
