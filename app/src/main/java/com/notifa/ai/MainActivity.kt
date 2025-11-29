package com.notifa.ai

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import com.notifa.ai.ai.GeminiService
import com.notifa.ai.data.AppDatabase
import com.notifa.ai.data.AppPreferenceRepository
import com.notifa.ai.data.BatchScheduleRepository
import com.notifa.ai.data.NotificationRepository
import com.notifa.ai.data.PreferencesManager
import com.notifa.ai.ui.MainScreen
import com.notifa.ai.ui.inbox.InboxViewModel
import com.notifa.ai.ui.onboarding.OnboardingScreen
import com.notifa.ai.ui.settings.AppCategoriesViewModel
import com.notifa.ai.ui.settings.BatchScheduleViewModel
import com.notifa.ai.ui.theme.NotifaTheme
import com.notifa.ai.util.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var geminiService: GeminiService
    private lateinit var repository: NotificationRepository
    private lateinit var inboxViewModel: InboxViewModel
    private lateinit var insightsViewModel: com.notifa.ai.ui.insights.InsightsViewModel
    private lateinit var batchScheduleViewModel: BatchScheduleViewModel
    private lateinit var appCategoriesViewModel: AppCategoriesViewModel
    private val requestPostNotifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // no-op; sticky summary only posts if we have permission
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Ensure channel exists
        NotificationHelper.createNotificationChannel(applicationContext)
        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPostNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Initialize dependencies
        preferencesManager = PreferencesManager(applicationContext)
        geminiService = GeminiService.getInstance()
        val database = AppDatabase.getDatabase(applicationContext)
        repository = NotificationRepository(database.notificationDao())
        inboxViewModel = InboxViewModel(repository, geminiService, preferencesManager, applicationContext)
        insightsViewModel = com.notifa.ai.ui.insights.InsightsViewModel(repository, applicationContext)
        val batchRepo = BatchScheduleRepository(database.batchScheduleDao())
        batchScheduleViewModel = BatchScheduleViewModel(batchRepo)
        val appPrefRepo = AppPreferenceRepository(database.appPreferenceDao())
        appCategoriesViewModel = AppCategoriesViewModel(appPrefRepo, packageManager)

        setContent {
            NotifaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
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
                                inboxViewModel = inboxViewModel,
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

    override fun onResume() {
        super.onResume()
        val hasPermission = isNotificationListenerEnabled()
        kotlinx.coroutines.GlobalScope.launch {
            preferencesManager.setNotificationPermissionGranted(hasPermission)
        }
        // If notifications exist, make sure summary is shown (InboxViewModel listener will update subsequently)
        // No-op here; channel creation in onCreate ensures readiness
    }

    private fun isNotificationListenerEnabled(): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
    }
}
