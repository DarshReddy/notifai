package com.notif.ai.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notif.ai.ai.GeminiService
import com.notif.ai.ai.NotificationData
import com.notif.ai.data.NotificationRepository
import com.notif.ai.util.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _isFocusModeEnabled = MutableStateFlow(false)
    val isFocusModeEnabled: StateFlow<Boolean> = _isFocusModeEnabled.asStateFlow()

    private val _primeTask = MutableStateFlow("Analyzing your priorities...")
    val primeTask: StateFlow<String> = _primeTask.asStateFlow()

    init {
        updatePrimeTask()
    }

    fun toggleFocusMode(enabled: Boolean) {
        _isFocusModeEnabled.value = enabled
        // In a real app, this would toggle DND or similar
    }

    fun updatePrimeTask() {
        viewModelScope.launch {
            repository.getNotificationsByPriority(Priority.MY_PRIORITY.name)
                .collectLatest { notifications ->
                    if (notifications.isEmpty()) {
                        _primeTask.value = "No urgent tasks. Enjoy your day!"
                    } else {
                        // Take top 10 most recent priority notifications for analysis
                        val recentPriority = notifications.take(10).map {
                            NotificationData(it.packageName, it.title, it.text)
                        }
                        _primeTask.value = GeminiService.generatePrimeTask(recentPriority)
                    }
                }
        }
    }
}
