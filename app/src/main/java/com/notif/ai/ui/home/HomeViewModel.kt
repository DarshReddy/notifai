package com.notif.ai.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {

    private val _isFocusModeEnabled = MutableStateFlow(false)
    val isFocusModeEnabled: StateFlow<Boolean> = _isFocusModeEnabled.asStateFlow()

    fun toggleFocusMode(enabled: Boolean) {
        _isFocusModeEnabled.value = enabled
        // TODO: Implement actual focus mode logic (e.g., mute notifications)
    }

    // Mock data for Prime Task
    val primeTask = "Confirm your 1 PM meeting and draft the follow-up email to Sarah."
}
