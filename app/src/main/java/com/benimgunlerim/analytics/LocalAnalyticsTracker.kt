package com.benimgunlerim.analytics

import android.util.Log
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class LocalAnalyticsTracker @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val scope: CoroutineScope,
) : AnalyticsTracker {

    override fun track(event: AnalyticsEvent) {
        scope.launch(Dispatchers.IO) {
            if (!preferencesRepository.preferences.first().analyticsEnabled) return@launch
            Log.d("BenimGunlerimAnalytics", "${event.name} ${event.properties}")
        }
    }
}
