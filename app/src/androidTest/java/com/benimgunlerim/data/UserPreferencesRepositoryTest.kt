package com.benimgunlerim.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.LightDayMode
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the four UserPreferencesRepository surfaces flagged by the audit as
 * critical and untested: idempotent reward granting, event-list pruning,
 * shop purchases, and Hafif Gün Modu enable/disable.
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {

    private lateinit var context: Context
    private val fixedDateTimeProvider = object : DateTimeProvider {
        var currentToday: LocalDate = LocalDate.of(2026, 8, 16)
        override fun today(): LocalDate = currentToday
        override fun currentTime(): LocalTime = LocalTime.of(12, 0)
        override fun currentTimeMillis(): Long = 1_000L
    }
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        // DataStore's Preferences implementation persists to this fixed path —
        // wipe it so each test starts from a clean, deterministic state.
        File(context.filesDir, "datastore/user_preferences.preferences_pb").delete()
        fixedDateTimeProvider.currentToday = LocalDate.of(2026, 8, 16)
        repository = UserPreferencesRepository(context, fixedDateTimeProvider)
    }

    // ── grantRewardOnce ─────────────────────────────────────────────────────

    @Test
    fun grantRewardOnce_sameEventKeyTwice_grantsOnlyOnce() = runTest {
        val first = repository.grantRewardOnce("task:123:2026-08-16", xp = 10, gold = 5)
        val second = repository.grantRewardOnce("task:123:2026-08-16", xp = 10, gold = 5)

        assertTrue(first)
        assertFalse(second)
        val prefs = repository.preferences.first()
        assertEquals(10, prefs.totalXp)
        assertEquals(5, prefs.gold)
    }

    @Test
    fun grantRewardOnce_differentEventKeys_bothGranted() = runTest {
        val first = repository.grantRewardOnce("task:123:2026-08-16", xp = 10)
        val second = repository.grantRewardOnce("task:456:2026-08-16", xp = 10)

        assertTrue(first)
        assertTrue(second)
        assertEquals(20, repository.preferences.first().totalXp)
    }

    // ── pruneRewardedEvents (exercised through grantRewardOnce) ─────────────

    @Test
    fun grantRewardOnce_agesOutEntriesOlderThanRetentionWindow() = runTest {
        val oldKey = "task:old:${fixedDateTimeProvider.today()}"
        repository.grantRewardOnce(oldKey, xp = 1)
        assertTrue(oldKey in repository.preferences.first().rewardedEvents)

        // Jump forward past the 90-day retention window and grant a new event —
        // pruning runs as a side effect of every grantRewardOnce call.
        fixedDateTimeProvider.currentToday = fixedDateTimeProvider.currentToday.plusDays(91)
        val newKey = "task:new:${fixedDateTimeProvider.today()}"
        repository.grantRewardOnce(newKey, xp = 1)

        val events = repository.preferences.first().rewardedEvents
        assertFalse("aged-out key should have been pruned", oldKey in events)
        assertTrue("fresh key should be retained", newKey in events)
    }

    @Test
    fun grantRewardOnce_permanentKeyWithoutDate_isNeverPrunedByAge() = runTest {
        repository.grantRewardOnce("achievement:first_task", xp = 5)

        fixedDateTimeProvider.currentToday = fixedDateTimeProvider.currentToday.plusDays(365)
        repository.grantRewardOnce("task:new:${fixedDateTimeProvider.today()}", xp = 1)

        assertTrue("non-date achievement key should survive pruning", "achievement:first_task" in repository.preferences.first().rewardedEvents)
    }

    // ── purchaseItem ──────────────────────────────────────────────────────

    @Test
    fun purchaseItem_insufficientGold_fails_andDoesNotChargeOrOwn() = runTest {
        val success = repository.purchaseItem("hat_blue", cost = 100)

        assertFalse(success)
        val prefs = repository.preferences.first()
        assertEquals(0, prefs.gold)
        assertFalse("hat_blue" in prefs.ownedItems.split(","))
    }

    @Test
    fun purchaseItem_sufficientGold_succeeds_deductsAndOwns() = runTest {
        repository.addXpAndGold(xp = 0, gold = 100, happinessDelta = 0)

        val success = repository.purchaseItem("hat_blue", cost = 60)

        assertTrue(success)
        val prefs = repository.preferences.first()
        assertEquals(40, prefs.gold)
        assertTrue("hat_blue" in prefs.ownedItems.split(","))
    }

    @Test
    fun purchaseItem_alreadyOwned_doesNotChargeAgain() = runTest {
        repository.addXpAndGold(xp = 0, gold = 200, happinessDelta = 0)
        repository.purchaseItem("hat_blue", cost = 60)

        val secondAttempt = repository.purchaseItem("hat_blue", cost = 60)

        assertFalse(secondAttempt)
        assertEquals(140, repository.preferences.first().gold)
    }

    // ── setLightDayMode ───────────────────────────────────────────────────

    @Test
    fun setLightDayMode_enabled_storesDate() = runTest {
        repository.setLightDayMode(enabled = true, dateStr = "2026-08-16")

        val prefs = repository.preferences.first()
        assertEquals("2026-08-16", prefs.lightDayModeDate)
        assertTrue(LightDayMode.isActiveOn(prefs.lightDayModeDate, LocalDate.of(2026, 8, 16)))
    }

    @Test
    fun setLightDayMode_disabled_clearsDate() = runTest {
        repository.setLightDayMode(enabled = true, dateStr = "2026-08-16")

        repository.setLightDayMode(enabled = false, dateStr = "2026-08-16")

        val prefs = repository.preferences.first()
        assertEquals("", prefs.lightDayModeDate)
        assertFalse(LightDayMode.isActiveOn(prefs.lightDayModeDate, LocalDate.of(2026, 8, 16)))
    }
}
