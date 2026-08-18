package com.benimgunlerim.domain

import androidx.annotation.StringRes
import com.benimgunlerim.R

/**
 * Oyunlaştırma motoru — XP, seviye, altın hesaplamaları.
 * REDESIGN.md Bölüm 3'teki tablolara göre tasarlandı.
 */
object GameEngine {

    // ── XP Ödülleri ───────────────────────────────────────────────────────
    const val XP_TASK_COMPLETE = 10   // legacy fallback
    const val XP_TASK_LOW = 8
    const val XP_TASK_NORMAL = 12
    const val XP_TASK_HIGH = 18
    const val XP_ROUTINE_COMPLETE = 15  // legacy fallback
    const val XP_ROUTINE_CHECKBOX = 10
    const val XP_ROUTINE_GOAL = 15
    const val XP_ALL_TASKS_BONUS = 25
    const val XP_ALL_ROUTINES_BONUS = 30
    const val XP_DAY_CLOSE = 20
    const val XP_PERFECT_DAY = 30
    const val XP_STREAK_7 = 50
    const val XP_STREAK_30 = 200

    // ── Altın Ödülleri ────────────────────────────────────────────────────
    const val GOLD_TASK_COMPLETE = 5
    const val GOLD_ROUTINE_COMPLETE = 8
    const val GOLD_PERFECT_DAY = 20
    const val GOLD_STREAK_7 = 30
    const val GOLD_STREAK_30 = 100

    // ── Mutluluk Değişimleri ──────────────────────────────────────────────
    const val HAPPINESS_TASK = 5
    const val HAPPINESS_ROUTINE = 8
    const val HAPPINESS_STREAK = 10
    const val HAPPINESS_MISS = -3
    const val HAPPINESS_MIN = 20
    const val HAPPINESS_MAX = 100

    // ── Seviye Sistemi ────────────────────────────────────────────────────

    data class LevelInfo(
        val level: Int,
        @StringRes val titleRes: Int,
        val currentXp: Int,
        val xpForNextLevel: Int,
        val totalXp: Int,
    )

    private val levelThresholds = listOf(
        100 to R.string.game_level_title_1,       // Seviye 1 (0-99 XP)
        100 to R.string.game_level_title_2,       // Seviye 2 (100-199 XP)
        100 to R.string.game_level_title_3,       // Seviye 3 (200-299 XP)
        100 to R.string.game_level_title_4,       // Seviye 4 (300-399 XP)
        100 to R.string.game_level_title_5,       // Seviye 5 (400-499 XP)
        100 to R.string.game_level_title_6,       // Seviye 6 (500-599 XP)
        100 to R.string.game_level_title_7,       // Seviye 7 (600-699 XP)
        100 to R.string.game_level_title_8,       // Seviye 8 (700-799 XP)
        100 to R.string.game_level_title_9,       // Seviye 9 (800-899 XP)
        100 to R.string.game_level_title_10,      // Seviye 10 (900-999 XP)
        200 to R.string.game_level_title_10,      // Seviye 11
        200 to R.string.game_level_title_efsane,  // Seviye 12+
    )

    fun calculateLevel(totalXp: Int): LevelInfo {
        var remaining = totalXp
        var level = 1
        var titleRes = R.string.game_level_title_1

        for ((threshold, t) in levelThresholds) {
            if (remaining < threshold) {
                return LevelInfo(level, titleRes, remaining, threshold, totalXp)
            }
            remaining -= threshold
            level++
            titleRes = t
        }
        // Level 30+: her seviye 2000 XP
        val extraLevels = remaining / 2000
        val leftover = remaining % 2000
        return LevelInfo(
            level = level + extraLevels,
            titleRes = R.string.game_level_title_gun_tanrisi,
            currentXp = leftover,
            xpForNextLevel = 2000,
            totalXp = totalXp,
        )
    }

    fun clampHappiness(value: Int): Int = value.coerceIn(HAPPINESS_MIN, HAPPINESS_MAX)

    fun companionMood(happiness: Int): String = when {
        happiness >= 80 -> "ecstatic"
        happiness >= 60 -> "happy"
        happiness >= 40 -> "neutral"
        else -> "sad"
    }

    fun companionEmoji(type: String, mood: String): String = when (type) {
        "fox"    -> when (mood) { "ecstatic" -> "🦊✨"; "sad" -> "🦊😢"; else -> "🦊" }
        "cat"    -> when (mood) { "ecstatic" -> "🐱✨"; "sad" -> "🐱😢"; else -> "🐱" }
        "rabbit" -> when (mood) { "ecstatic" -> "🐰✨"; "sad" -> "🐰😢"; else -> "🐰" }
        "owl"    -> when (mood) { "ecstatic" -> "🦉✨"; "sad" -> "🦉😢"; else -> "🦉" }
        else     -> "🐱"
    }

    /** [CompanionMessage.Simple] tek bir string kaynağı taşır; [CompanionMessage.Streak] ise
     * seri sayısıyla birlikte format string'i taşır (%1$d placeholder). Henüz gerçek bir UI
     * çağıranı yok — string kaynağı hazır bekletiliyor. */
    sealed class CompanionMessage {
        data class Simple(@StringRes val textRes: Int) : CompanionMessage()
        data class Streak(@StringRes val formatRes: Int, val streak: Int) : CompanionMessage()
    }

    fun companionMessage(
        mood: String,
        streak: Int,
        progress: Float,
        random: RandomProvider = SystemRandomProvider(),
    ): CompanionMessage = when {
        progress >= 1f -> CompanionMessage.Simple(
            random.pickFrom(
                listOf(
                    R.string.companion_msg_full_progress_1,
                    R.string.companion_msg_full_progress_2,
                    R.string.companion_msg_full_progress_3,
                ),
            ),
        )
        mood == "ecstatic" -> CompanionMessage.Simple(
            random.pickFrom(
                listOf(
                    R.string.companion_msg_ecstatic_1,
                    R.string.companion_msg_ecstatic_2,
                    R.string.companion_msg_ecstatic_3,
                ),
            ),
        )
        mood == "happy" -> CompanionMessage.Simple(
            random.pickFrom(
                listOf(
                    R.string.companion_msg_happy_1,
                    R.string.companion_msg_happy_2,
                    R.string.companion_msg_happy_3,
                ),
            ),
        )
        streak >= 7 -> CompanionMessage.Streak(R.string.companion_msg_streak_format, streak)
        progress >= 0.5f -> CompanionMessage.Simple(
            random.pickFrom(listOf(R.string.companion_msg_half_progress_1, R.string.companion_msg_half_progress_2)),
        )
        progress > 0f -> CompanionMessage.Simple(
            random.pickFrom(listOf(R.string.companion_msg_small_progress_1, R.string.companion_msg_small_progress_2)),
        )
        else -> CompanionMessage.Simple(
            random.pickFrom(
                listOf(
                    R.string.companion_msg_zero_progress_1,
                    R.string.companion_msg_zero_progress_2,
                    R.string.companion_msg_zero_progress_3,
                ),
            ),
        )
    }

    fun xpForTask(priority: Int): Int = when (priority) {
        1    -> XP_TASK_LOW
        3    -> XP_TASK_HIGH
        else -> XP_TASK_NORMAL
    }

    fun xpForRoutine(targetType: String): Int = when (targetType) {
        "check" -> XP_ROUTINE_CHECKBOX
        else    -> XP_ROUTINE_GOAL
    }
}
