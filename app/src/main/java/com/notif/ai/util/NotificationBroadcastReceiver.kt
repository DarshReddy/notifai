package com.notif.ai.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.notif.ai.CATEGORY_CLICK") {
            val category = intent.getStringExtra("category")
            if (category != null) {
                NotificationHelper.onCategoryClick(context, category)
            }
        }
    }
}
