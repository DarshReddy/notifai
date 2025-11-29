package com.notifa.ai.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifa.ai.data.NotificationEntity
import com.notifa.ai.data.NotificationCategory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    val lastBatchTime by viewModel.lastBatchTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Calculate next batch time (simple: every 2 hours)
    val nextBatchTime = remember(lastBatchTime) {
        if (lastBatchTime == 0L) {
            System.currentTimeMillis() + (2 * 60 * 60 * 1000)
        } else {
            lastBatchTime + (2 * 60 * 60 * 1000)
        }
    }

    val timeUntilBatch = remember(nextBatchTime) {
        val diff = nextBatchTime - System.currentTimeMillis()
        val minutes = (diff / 1000 / 60).toInt()
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        if (hours > 0) "${hours}h ${remainingMinutes}m" else "${remainingMinutes}m"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inbox",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAll() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "All caught up!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No notifications to show",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Batch timer card
                item {
                    BatchTimerCard(timeUntilBatch = timeUntilBatch)
                }

                // Group notifications by category
                val instantNotifications = notifications.filter { it.category == NotificationCategory.INSTANT }
                val batchedNotifications = notifications.filter { it.category == NotificationCategory.BATCHED }

                if (instantNotifications.isNotEmpty()) {
                    item {
                        Text(
                            "Instant",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(instantNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onSummarize = { viewModel.summarizeNotification(notification) },
                            onDelete = { viewModel.deleteNotification(notification.id) },
                            onMarkRead = { viewModel.markAsRead(notification.id) },
                            isLoading = isLoading
                        )
                    }
                }

                if (batchedNotifications.isNotEmpty()) {
                    item {
                        Text(
                            "Batched (${batchedNotifications.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(batchedNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onSummarize = { viewModel.summarizeNotification(notification) },
                            onDelete = { viewModel.deleteNotification(notification.id) },
                            onMarkRead = { viewModel.markAsRead(notification.id) },
                            isLoading = isLoading
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatchTimerCard(timeUntilBatch: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Next batch in",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        timeUntilBatch,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: NotificationEntity,
    onSummarize: () -> Unit,
    onDelete: () -> Unit,
    onMarkRead: () -> Unit,
    isLoading: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm • MMM dd", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Priority indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = when (notification.priority.name) {
                                    "HIGH" -> Color(0xFFEF4444)
                                    "LOW" -> Color(0xFF10B981)
                                    else -> Color(0xFF3B82F6)
                                },
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        notification.packageName.split(".").lastOrNull() ?: notification.packageName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    dateFormat.format(Date(notification.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                notification.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content
            Text(
                notification.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2
            )

            // AI Summary
            if (notification.isSummarized && !notification.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            notification.summary,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Actions
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!notification.isSummarized) {
                    TextButton(
                        onClick = onSummarize,
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Summarize", fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

