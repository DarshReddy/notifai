package com.notif.ai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notif.ai.data.BatchSchedule
import com.notif.ai.data.BatchScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatchScheduleViewModel(private val repo: BatchScheduleRepository) : ViewModel() {
    val schedules: StateFlow<List<BatchSchedule>> =
        repo.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun add(minutes: Int) {
        viewModelScope.launch { repo.add(minutes) }
    }

    fun update(id: Int, minutes: Int, enabled: Boolean = true) {
        viewModelScope.launch { repo.update(id, minutes, enabled) }
    }

    fun delete(id: Int) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun clear() {
        viewModelScope.launch { repo.clear() }
    }
}
