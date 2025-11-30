package com.notif.ai.ui.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notif.ai.data.PreferencesManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    val notificationGranted by preferencesManager.notificationPermissionGranted.collectAsState(
        initial = false
    )
    val usageGranted by preferencesManager.usageStatsPermissionGranted.collectAsState(initial = false)

    val pages = listOf(
        OnboardingPageData(
            title = "Welcome to Notifa",
            description = "Your AI-powered notification manager. Batch, summarize, and take control of your notifications.",
            icon = Icons.Default.Notifications,
            gradient = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        ),
        OnboardingPageData(
            title = "Undisturbed Mode",
            description = "Group notifications and receive them at scheduled times. Stay focused without missing what's important.",
            icon = Icons.Default.AccessTime,
            gradient = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
        ),
        OnboardingPageData(
            title = "AI Summaries",
            description = "Get intelligent summaries of your notifications powered by Gemini AI. Save time and stay informed.",
            icon = Icons.Default.Star,
            gradient = listOf(Color(0xFFEC4899), Color(0xFFF43F5E))
        ),
        OnboardingPageData(
            title = "Grant Permissions",
            description = "Notifa needs notification access to manage your notifications intelligently.",
            icon = Icons.Default.Security,
            gradient = listOf(Color(0xFFF43F5E), Color(0xFFF59E0B))
        ),
        OnboardingPageData(
            title = "Notification Access",
            description = "Grant access so Notifa can batch & summarize notifications.",
            icon = Icons.Default.NotificationsActive,
            gradient = listOf(Color(0xFF6366F1), Color(0xFF10B981))
        ),
        OnboardingPageData(
            title = "Usage Stats",
            description = "Allow usage access to generate accurate insights (optional).",
            icon = Icons.Default.BarChart,
            gradient = listOf(Color(0xFF10B981), Color(0xFFF59E0B))
        ),
        OnboardingPageData(
            title = "Finish Setup",
            description = "You're ready! Explore Inbox, Insights and Settings.",
            icon = Icons.Default.CheckCircle,
            gradient = listOf(Color(0xFFF59E0B), Color(0xFF6366F1))
        )
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        scope.launch {
                            when (pagerState.currentPage) {
                                pages.lastIndex -> {
                                    preferencesManager.setOnboardingCompleted(true)
                                    onComplete()
                                }

                                pages.indexOfFirst { it.title == "Notification Access" } -> {
                                    val intent =
                                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                }

                                pages.indexOfFirst { it.title == "Usage Stats" } -> {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    context.startActivity(intent)
                                }

                                else -> pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    val label = when (pagerState.currentPage) {
                        pages.lastIndex -> "Get Started"
                        pages.indexOfFirst { it.title == "Notification Access" } -> if (notificationGranted) "Next" else "Grant"
                        pages.indexOfFirst { it.title == "Usage Stats" } -> if (usageGranted) "Next" else "Grant"
                        else -> "Next"
                    }
                    Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        if (pagerState.currentPage == pages.size - 1)
                            Icons.Default.Check
                        else
                            Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next"
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with gradient background
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.linearGradient(page.gradient),
                    shape = RoundedCornerShape(30.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            lineHeight = 28.sp
        )
    }
}

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>
)
