package com.notif.ai.ui.settings

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notif.ai.data.AppPreference
import com.notif.ai.data.AppPreferenceRepository
import com.notif.ai.data.NotificationCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppCategoriesViewModel(
    private val repo: AppPreferenceRepository,
    private val pm: PackageManager
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppPreference>>(emptyList())
    val apps: StateFlow<List<AppPreference>> = _apps.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            val existing = repo.getAll().first()
            if (existing.isEmpty()) {
                // seed from launchable apps
                val launchables = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                    .take(100) // limit
                    .map { appInfo ->
                        AppPreference(
                            packageName = appInfo.packageName,
                            appName = pm.getApplicationLabel(appInfo).toString(),
                            category = NotificationCategory.BATCHED,
                            isEnabled = true
                        )
                    }
                repo.upsertAll(launchables)
                _apps.value = launchables.sortedBy { it.appName.lowercase() }
            } else {
                _apps.value = existing.sortedBy { it.appName.lowercase() }
            }
            _loading.value = false
        }
    }

    fun toggleCategory(packageName: String) {
        viewModelScope.launch {
            val current = repo.get(packageName)
            if (current != null) {
                val newCat =
                    if (current.category == NotificationCategory.BATCHED) NotificationCategory.INSTANT else NotificationCategory.BATCHED
                repo.upsert(current.copy(category = newCat))
                _apps.value =
                    _apps.value.map { if (it.packageName == packageName) it.copy(category = newCat) else it }
            }
        }
    }
}

