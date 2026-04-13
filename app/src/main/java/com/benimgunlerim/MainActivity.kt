package com.benimgunlerim

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.onboarding.OnboardingViewModel
import com.benimgunlerim.ui.BenimGunlerimApp
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import com.benimgunlerim.notifications.NotificationConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val preferences by onboardingViewModel.preferences.collectAsState()
            BenimGunlerimTheme(themeMode = preferences.themeMode) {
                BenimGunlerimApp(
                    requestedStartRoute = intent.getStringExtra(NotificationConstants.EXTRA_START_ROUTE),
                )
            }
        }
    }
}
