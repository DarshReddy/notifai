package com.notifa.ai.ui.onboarding

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.accompanist.pager.*
import com.notifa.ai.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }

    val notificationGranted by preferencesManager.notificationPermissionGranted.collectAsState(initial = false)
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
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            // Indicators
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = MaterialTheme.colorScheme.primary,
                inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
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
                            Icons.Default.ArrowForward,
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
