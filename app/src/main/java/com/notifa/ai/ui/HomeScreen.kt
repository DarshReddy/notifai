package com.notifa.ai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.notifa.ai.data.NotificationEntity
import com.notifa.ai.util.Priority
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: NotificationListViewModel,
    modifier: Modifier = Modifier
) {
    val notifications by viewModel.notifications.collectAsState()
    val filter by viewModel.filter.collectAsState()

    Column(modifier = modifier.padding(16.dp)) {
        FilterButtons(
            currentFilter = filter,
            onFilterChanged = { viewModel.setFilter(it) }
        )
        LazyColumn {
            items(notifications) { notification ->
                NotificationItem(notification = notification)
            }
        }
    }
}

@Composable
fun FilterButtons(
    currentFilter: Priority?,
    onFilterChanged: (Priority?) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { onFilterChanged(null) }, enabled = currentFilter != null) {
            Text("All")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onFilterChanged(Priority.HIGH) }, enabled = currentFilter != Priority.HIGH) {
            Text("High")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onFilterChanged(Priority.NORMAL) }, enabled = currentFilter != Priority.NORMAL) {
            Text("Normal")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onFilterChanged(Priority.LOW) }, enabled = currentFilter != Priority.LOW) {
            Text("Low")
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationEntity) {
    Card(modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = notification.title, style = androidx.compose.ui.text.font.FontWeight.Bold.let { androidx.compose.ui.text.TextStyle(fontWeight = it) })
            Text(text = notification.text)
            Text(text = "From: ${notification.packageName}")
            Text(text = "Priority: ${notification.priority}")
            Text(text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(notification.timestamp)))
        }
    }
}
