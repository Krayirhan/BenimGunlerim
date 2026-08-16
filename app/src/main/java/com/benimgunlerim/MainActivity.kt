package com.benimgunlerim

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.benimgunlerim.ui.onboarding.OnboardingViewModel
import com.benimgunlerim.ui.navigation.BenimGunlerimApp
import com.benimgunlerim.ui.theme.BenimGunlerimTheme
import com.benimgunlerim.notifications.NotificationConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private companion object {
        const val EXTRA_FORCE_ONBOARDING_COMPLETED = "force_onboarding_completed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge kullanıldığı için XML'deki status bar ayarları tek başına
        // yeterli değildir. Açık modda sistem ikonlarını özellikle koyu tutuyoruz.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val preferences by onboardingViewModel.preferences.collectAsState()
            val forceOnboardingCompleted = BuildConfig.DEBUG &&
                intent.getBooleanExtra(EXTRA_FORCE_ONBOARDING_COMPLETED, false)
            BenimGunlerimTheme(themeMode = preferences.themeMode) {
                BenimGunlerimApp(
                    requestedStartRoute = intent.getStringExtra(NotificationConstants.EXTRA_START_ROUTE),
                    forceOnboardingCompleted = forceOnboardingCompleted,
                )
            }
        }
    }
}
