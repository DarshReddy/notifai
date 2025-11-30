package com.notif.ai.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchScheduleScreen(onBack: () -> Unit, viewModel: BatchScheduleViewModel) {
    val schedules = viewModel.schedules.collectAsState()
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var hour by remember { mutableIntStateOf(9) }
    var minute by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Batch Schedule", fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val existing = schedules.value?.map { it.timeInMinutes }?.toSet()
                val maxTime = 23 * 60 + 59 // 23:59 is the absolute max

                // Start from last slot + 2 hours, or default to 9:00
                val lastSlot = existing?.maxOrNull() ?: (7 * 60)
                var candidate = lastSlot + 120

                // If beyond end of day, wrap to morning starting at 6:00
                if (candidate > maxTime) {
                    candidate = 6 * 60
                }

                // Find next free slot (search forward in 30-min increments)
                var searchAttempts = 0
                val maxAttempts = 48 // max 24 hours in 30-min slots

                while (existing?.contains(candidate) == true && searchAttempts < maxAttempts) {
                    candidate += 30
                    // Wrap around if we exceed max time
                    if (candidate > maxTime) {
                        candidate = 6 * 60
                    }
                    searchAttempts++
                }

                // Only add if we found a valid unique slot within bounds
                if (existing?.contains(candidate) != true) {
                    viewModel.add(candidate)
                }
            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(schedules.value.size) { idx ->
                val item = schedules.value[idx]
                val minutes = item.timeInMinutes
                val h = minutes / 60
                val m = minutes % 60
                Card(onClick = { editingIndex = idx; hour = h; minute = m }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AssistChip(onClick = {
                                viewModel.update(
                                    item.id,
                                    minutes,
                                    !item.isEnabled
                                )
                            }, label = { Text(if (item.isEnabled) "Enabled" else "Disabled") })
                            AssistChip(
                                onClick = { viewModel?.delete(item.id) },
                                label = { Text("Delete") })
                        }
                    }
                }
            }
        }
        if (editingIndex != null) {
            AlertDialog(
                onDismissRequest = { editingIndex = null },
                title = { Text("Edit Batch Time") },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = { v ->
                                v.toIntOrNull()?.let { hour = it.coerceIn(0, 23) }
                            },
                            label = { Text("Hour") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                        OutlinedTextField(
                            value = minute.toString(),
                            onValueChange = { v ->
                                v.toIntOrNull()?.let { minute = it.coerceIn(0, 59) }
                            },
                            label = { Text("Min") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val newVal = hour * 60 + minute
                        val item = schedules.value.getOrNull(editingIndex!!)
                        if (item != null) viewModel.update(item.id, newVal, item.isEnabled)
                        editingIndex = null
                    }) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = { editingIndex = null }) { Text("Cancel") } }
            )
        }
    }
}
