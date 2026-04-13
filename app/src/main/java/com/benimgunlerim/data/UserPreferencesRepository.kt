package com.benimgunlerim.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal val Context.userPreferencesDataStore by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val selectedGoalProfile: String? = null,
    val notificationMode: String = "light",
    val dailySummaryTime: String = "21:00",
    val analyticsEnabled: Boolean = true,
    val themeMode: String = "system",
    // Game state
    val totalXp: Int = 0,
    val gold: Int = 0,
    val happiness: Int = 0,
    val companionType: String = "cat",
    val companionName: String = "Pati",
    // Daily reward
    val lastDailyRewardDate: String = "",
    // Stats
    val totalTasksCompleted: Int = 0,
    val totalRoutinesCompleted: Int = 0,
    val totalPerfectDays: Int = 0,
    val totalDaysClosed: Int = 0,
    val happyMoodCount: Int = 0,
    // Shop
    val ownedItems: String = "",  // comma-separated item IDs
    val rewardedEvents: String = "",
    // Sprint 8 — Notification 2.0
    val morningPlannerEnabled: Boolean = false,
    val morningPlannerTime: String = "08:00",
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
        val selectedGoalProfile = stringPreferencesKey("selected_goal_profile")
        val notificationMode = stringPreferencesKey("notification_mode")
        val dailySummaryTime = stringPreferencesKey("daily_summary_time")
        val analyticsEnabled = booleanPreferencesKey("analytics_enabled")
        val themeMode = stringPreferencesKey("theme_mode")
        val totalXp = intPreferencesKey("total_xp")
        val gold = intPreferencesKey("gold")
        val happiness = intPreferencesKey("happiness")
        val companionType = stringPreferencesKey("companion_type")
        val companionName = stringPreferencesKey("companion_name")
        val lastDailyRewardDate = stringPreferencesKey("last_daily_reward_date")
        val totalTasksCompleted = intPreferencesKey("total_tasks_completed")
        val totalRoutinesCompleted = intPreferencesKey("total_routines_completed")
        val totalPerfectDays = intPreferencesKey("total_perfect_days")
        val totalDaysClosed = intPreferencesKey("total_days_closed")
        val happyMoodCount = intPreferencesKey("happy_mood_count")
        val ownedItems = stringPreferencesKey("owned_items")
        val rewardedEvents = stringPreferencesKey("rewarded_events")
        val morningPlannerEnabled = booleanPreferencesKey("morning_planner_enabled")
        val morningPlannerTime = stringPreferencesKey("morning_planner_time")
        val quietHoursEnabled = booleanPreferencesKey("quiet_hours_enabled")
        val quietHoursStart = stringPreferencesKey("quiet_hours_start")
        val quietHoursEnd = stringPreferencesKey("quiet_hours_end")
    }

    val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            onboardingCompleted = prefs[Keys.onboardingCompleted] ?: false,
            selectedGoalProfile = prefs[Keys.selectedGoalProfile],
            notificationMode = prefs[Keys.notificationMode] ?: "light",
            dailySummaryTime = prefs[Keys.dailySummaryTime] ?: "21:00",
            analyticsEnabled = prefs[Keys.analyticsEnabled] ?: true,
            themeMode = prefs[Keys.themeMode] ?: "system",
            totalXp = prefs[Keys.totalXp] ?: 0,
            gold = prefs[Keys.gold] ?: 0,
            happiness = prefs[Keys.happiness] ?: 0,
            companionType = prefs[Keys.companionType] ?: "cat",
            companionName = prefs[Keys.companionName] ?: "Pati",
            lastDailyRewardDate = prefs[Keys.lastDailyRewardDate] ?: "",
            totalTasksCompleted = prefs[Keys.totalTasksCompleted] ?: 0,
            totalRoutinesCompleted = prefs[Keys.totalRoutinesCompleted] ?: 0,
            totalPerfectDays = prefs[Keys.totalPerfectDays] ?: 0,
            totalDaysClosed = prefs[Keys.totalDaysClosed] ?: 0,
            happyMoodCount = prefs[Keys.happyMoodCount] ?: 0,
            ownedItems = prefs[Keys.ownedItems] ?: "",
            rewardedEvents = prefs[Keys.rewardedEvents] ?: "",
            morningPlannerEnabled = prefs[Keys.morningPlannerEnabled] ?: false,
            morningPlannerTime = prefs[Keys.morningPlannerTime] ?: "08:00",
            quietHoursEnabled = prefs[Keys.quietHoursEnabled] ?: false,
            quietHoursStart = prefs[Keys.quietHoursStart] ?: "22:00",
            quietHoursEnd = prefs[Keys.quietHoursEnd] ?: "07:00",
        )
    }

    suspend fun completeOnboarding(goalProfile: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.onboardingCompleted] = true
            prefs[Keys.selectedGoalProfile] = goalProfile
        }
    }

    suspend fun setNotificationMode(mode: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.notificationMode] = mode
        }
    }

    suspend fun setDailySummaryTime(time: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.dailySummaryTime] = time
        }
    }

    suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.analyticsEnabled] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.themeMode] = mode
        }
    }

    suspend fun resetOnboarding() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.onboardingCompleted] = false
        }
    }

    suspend fun addXp(amount: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.totalXp] = (prefs[Keys.totalXp] ?: 0) + amount
        }
    }

    suspend fun addGold(amount: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.gold] = (prefs[Keys.gold] ?: 0) + amount
        }
    }

    suspend fun setHappiness(value: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.happiness] = value.coerceIn(0, 100)
        }
    }

    suspend fun setCompanion(type: String, name: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.companionType] = type
            prefs[Keys.companionName] = name
        }
    }

    suspend fun addXpAndGold(xp: Int, gold: Int, happinessDelta: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.totalXp] = (prefs[Keys.totalXp] ?: 0) + xp
            prefs[Keys.gold] = (prefs[Keys.gold] ?: 0) + gold
            val current = prefs[Keys.happiness] ?: 0
            prefs[Keys.happiness] = (current + happinessDelta).coerceIn(0, 100)
        }
    }

    suspend fun grantRewardOnce(
        eventKey: String,
        xp: Int,
        gold: Int = 0,
        happinessDelta: Int = 0,
    ): Boolean {
        var granted = false
        context.userPreferencesDataStore.edit { prefs ->
            val currentEvents = prefs[Keys.rewardedEvents].orEmpty()
            val events = currentEvents.split(",").filter { it.isNotBlank() }.toSet()
            if (eventKey !in events) {
                prefs[Keys.rewardedEvents] = (events + eventKey).joinToString(",")
                prefs[Keys.totalXp] = (prefs[Keys.totalXp] ?: 0) + xp
                prefs[Keys.gold] = (prefs[Keys.gold] ?: 0) + gold
                val currentHappiness = prefs[Keys.happiness] ?: 0
                prefs[Keys.happiness] = (currentHappiness + happinessDelta).coerceIn(0, 100)
                granted = true
            }
        }
        return granted
    }

    suspend fun incrementTasksCompleted() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.totalTasksCompleted] = (prefs[Keys.totalTasksCompleted] ?: 0) + 1
        }
    }

    suspend fun incrementRoutinesCompleted() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.totalRoutinesCompleted] = (prefs[Keys.totalRoutinesCompleted] ?: 0) + 1
        }
    }

    suspend fun incrementPerfectDays() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.totalPerfectDays] = (prefs[Keys.totalPerfectDays] ?: 0) + 1
        }
    }

    suspend fun incrementDaysClosed() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.totalDaysClosed] = (prefs[Keys.totalDaysClosed] ?: 0) + 1
        }
    }

    suspend fun incrementHappyMoodCount() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.happyMoodCount] = (prefs[Keys.happyMoodCount] ?: 0) + 1
        }
    }

    suspend fun claimDailyReward(dateStr: String, goldAmount: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.lastDailyRewardDate] = dateStr
            prefs[Keys.gold] = (prefs[Keys.gold] ?: 0) + goldAmount
        }
    }

    suspend fun purchaseItem(itemId: String, cost: Int): Boolean {
        var success = false
        context.userPreferencesDataStore.edit { prefs ->
            val currentGold = prefs[Keys.gold] ?: 0
            if (currentGold >= cost) {
                prefs[Keys.gold] = currentGold - cost
                val owned = prefs[Keys.ownedItems] ?: ""
                prefs[Keys.ownedItems] = if (owned.isEmpty()) itemId else "$owned,$itemId"
                success = true
            }
        }
        return success
    }

    suspend fun setMorningPlannerEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.morningPlannerEnabled] = enabled
        }
    }

    suspend fun setMorningPlannerTime(time: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.morningPlannerTime] = time
        }
    }

    suspend fun setQuietHoursEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.quietHoursEnabled] = enabled
        }
    }

    suspend fun setQuietHoursStart(time: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.quietHoursStart] = time
        }
    }

    suspend fun setQuietHoursEnd(time: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[Keys.quietHoursEnd] = time
        }
    }
}
