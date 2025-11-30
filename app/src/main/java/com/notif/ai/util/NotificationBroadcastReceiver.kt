package com.notif.ai.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.notif.ai.CATEGORY_CLICK") {
            val category = intent.getStringExtra("category")
            // You can handle the click event here, for example, by opening the app with a specific filter
            Toast.makeText(context, "Clicked on category: $category", Toast.LENGTH_SHORT).show()
        }
    }
}


