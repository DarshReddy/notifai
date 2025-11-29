package com.notifa.ai.ui.settings

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifa.ai.data.NotificationCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCategoriesScreen(onBack: () -> Unit, viewModel: AppCategoriesViewModel? = null) {
    val context = LocalContext.current
    val loading = viewModel?.loading?.collectAsState() ?: mutableStateOf(false)
    val apps = viewModel?.apps?.collectAsState() ?: mutableStateOf(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Categories", fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        if (loading.value) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Tap to toggle Instant vs Batched",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                items(apps.value) { pref ->
                    Card(onClick = { viewModel?.toggleCategory(pref.packageName) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(pref.appName, fontWeight = FontWeight.Medium)
                                Text(pref.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                            AssistChip(
                                onClick = { viewModel?.toggleCategory(pref.packageName) },
                                label = { Text(if (pref.category == NotificationCategory.INSTANT) "Instant" else "Batched") }
                            )
                        }
                    }
                }
            }
        }
    }
}
