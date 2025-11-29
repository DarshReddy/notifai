package com.notifa.ai.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("notifa_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }

    var isOnboardingComplete: Boolean
        get() = sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = sharedPreferences.edit { putBoolean(KEY_ONBOARDING_COMPLETE, value) }
}
