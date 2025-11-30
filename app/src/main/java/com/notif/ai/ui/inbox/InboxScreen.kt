package com.notif.ai.ui.inbox

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.notif.ai.ui.NotificationListViewModel
import com.notif.ai.util.Priority

@Composable
fun InboxScreen(viewModel: NotificationListViewModel) {
    val myPriorityCount by viewModel.myPriorityCount.collectAsState()
    val importantCount by viewModel.importantCount.collectAsState()
    val promotionalCount by viewModel.promotionalCount.collectAsState()
    val spamCount by viewModel.spamCount.collectAsState()

    Column {
        NotificationCategoryItem(
            icon = Icons.Default.Star,
            title = "My Priority",
            count = myPriorityCount,
            onClick = { viewModel.setFilter(Priority.MY_PRIORITY) },
            iconColor = Color.Red,
            iconBackgroundColor = Color.Red.copy(alpha = 0.1f)
        )
        NotificationCategoryItem(
            icon = Icons.Default.Email,
            title = "Important",
            count = importantCount,
            onClick = { viewModel.setFilter(Priority.IMPORTANT) },
            iconColor = Color.Blue,
            iconBackgroundColor = Color.Blue.copy(alpha = 0.1f)
        )
        NotificationCategoryItem(
            icon = Icons.Default.LocalOffer,
            title = "Promotional",
            count = promotionalCount,
            onClick = { viewModel.setFilter(Priority.PROMOTIONAL) },
            iconColor = Color.Green,
            iconBackgroundColor = Color.Green.copy(alpha = 0.1f)
        )
        NotificationCategoryItem(
            icon = Icons.Default.Report,
            title = "Spam",
            count = spamCount,
            onClick = { viewModel.setFilter(Priority.SPAM) },
            iconColor = Color.Gray,
            iconBackgroundColor = Color.Gray.copy(alpha = 0.1f)
        )
    }
}

