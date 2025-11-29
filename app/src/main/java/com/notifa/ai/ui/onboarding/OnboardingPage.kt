package com.notifa.ai.ui.onboarding

sealed class OnboardingPage {
    object Welcome : OnboardingPage()
    object Permissions : OnboardingPage()
    object AppSelection : OnboardingPage()
    object Complete : OnboardingPage()
}

