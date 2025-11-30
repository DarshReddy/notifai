package com.notif.ai

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.notif.ai.data.AppDatabase
import com.notif.ai.data.AppPreferenceRepository
import com.notif.ai.data.BatchScheduleRepository
import com.notif.ai.data.NotificationRepository
import com.notif.ai.data.PreferencesManager
import com.notif.ai.ui.MainScreen
import com.notif.ai.ui.NotificationListViewModel
import com.notif.ai.ui.onboarding.OnboardingScreen
import com.notif.ai.ui.settings.AppCategoriesViewModel
import com.notif.ai.ui.settings.BatchScheduleViewModel
import com.notif.ai.ui.theme.NotifaTheme
import com.notif.ai.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var repository: NotificationRepository
    private lateinit var notificationListViewModel: NotificationListViewModel
    private lateinit var insightsViewModel: com.notif.ai.ui.insights.InsightsViewModel
    private lateinit var batchScheduleViewModel: BatchScheduleViewModel
    private lateinit var appCategoriesViewModel: AppCategoriesViewModel
    private val requestPostNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // no-op; sticky summary only posts if we have permission
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                Color.TRANSPARENT
            )
        )
        // Ensure channel exists
        NotificationHelper.createNotificationChannel(applicationContext)
        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Initialize dependencies
        preferencesManager = PreferencesManager(applicationContext)
        val database = AppDatabase.getDatabase(applicationContext)
        repository = NotificationRepository(database.notificationDao())
        notificationListViewModel = NotificationListViewModel(repository)
        insightsViewModel =
            com.notif.ai.ui.insights.InsightsViewModel(repository, applicationContext)
        val batchRepo = BatchScheduleRepository(database.batchScheduleDao())
        batchScheduleViewModel = BatchScheduleViewModel(batchRepo)
        val appPrefRepo = AppPreferenceRepository(database.appPreferenceDao())
        appCategoriesViewModel = AppCategoriesViewModel(appPrefRepo, packageManager)

        setContent {
            NotifaTheme {
                Scaffold {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val scope = rememberCoroutineScope()
                        var showOnboarding by remember { mutableStateOf<Boolean?>(null) }

                        LaunchedEffect(Unit) {
                            scope.launch {
                                val completed = preferencesManager.onboardingCompleted.first()
                                showOnboarding = !completed
                            }
                        }

                        when (showOnboarding) {
                            true -> {
                                OnboardingScreen(
                                    onComplete = {
                                        showOnboarding = false
                                    }
                                )
                            }

                            false -> {
                                MainScreen(
                                    notificationListViewModel = notificationListViewModel,
                                    insightsViewModel = insightsViewModel,
                                    batchScheduleViewModel = batchScheduleViewModel,
                                    appCategoriesViewModel = appCategoriesViewModel
                                )
                            }

                            null -> {
                                // Loading state
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val hasPermission = isNotificationListenerEnabled()
        lifecycleScope.launch {
            preferencesManager.setNotificationPermissionGranted(hasPermission)
        }
        // If notifications exist, make sure summary is shown (InboxViewModel listener will update subsequently)
        // No-op here; channel creation in onCreate ensures readiness
    }

    private fun isNotificationListenerEnabled(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }
}
