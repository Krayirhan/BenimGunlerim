package com.benimgunlerim.analytics

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}

data class AnalyticsEvent(
    val name: String,
    val properties: Map<String, String> = emptyMap(),
)
