package com.benimgunlerim.domain

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
        val title: String,
        val currentXp: Int,
        val xpForNextLevel: Int,
        val totalXp: Int,
    )

    private val levelThresholds = listOf(
        100 to "Çaylak",
        200 to "Başlangıç",
        350 to "Alışkanlık Avcısı",
        350 to "Alışkanlık Avcısı",
        500 to "Rutin Ustası",
        500 to "Rutin Ustası",
        500 to "Rutin Ustası",
        500 to "Rutin Ustası",
        500 to "Rutin Ustası",
        750 to "Günlük Kahraman",
        750 to "Günlük Kahraman",
        750 to "Günlük Kahraman",
        750 to "Günlük Kahraman",
        750 to "Günlük Kahraman",
        1000 to "Zaman Lordu",
        1000 to "Zaman Lordu",
        1000 to "Zaman Lordu",
        1000 to "Zaman Lordu",
        1000 to "Zaman Lordu",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
        1500 to "Efsane",
    )

    fun calculateLevel(totalXp: Int): LevelInfo {
        var remaining = totalXp
        var level = 1
        var title = "Çaylak"

        for ((threshold, t) in levelThresholds) {
            if (remaining < threshold) {
                return LevelInfo(level, title, remaining, threshold, totalXp)
            }
            remaining -= threshold
            level++
            title = t
        }
        // Level 30+: her seviye 2000 XP
        val extraLevels = remaining / 2000
        val leftover = remaining % 2000
        return LevelInfo(
            level = level + extraLevels,
            title = "Gün Tanrısı",
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

    fun companionMessage(
        mood: String,
        streak: Int,
        progress: Float,
        random: RandomProvider = SystemRandomProvider(),
    ): String = when {
        progress >= 1f     -> random.pickFrom(listOf("Bugün efsaneydin! 🎉", "Mükemmel gün! Her şeyi bitirdin! 🏆", "Sen bir kahraman! ⭐"))
        mood == "ecstatic" -> random.pickFrom(listOf("Harika gidiyorsun! 🥳", "Seninle çok mutluyum! 💜", "Birlikte her şeyi başarırız! ✨"))
        mood == "happy"    -> random.pickFrom(listOf("Güzel gidiyor! 💪", "Devam et, iyi yoldasın! 🌟", "Birlikte yapabiliriz! 😊"))
        streak >= 7        -> "Harika bir seri! $streak gün! 🔥"
        progress >= 0.5f   -> random.pickFrom(listOf("Yarıyı geçtin! 💫", "Güzel ilerliyorsun! 🌈"))
        progress > 0f           -> random.pickFrom(listOf("Güzel başlangıç! 🌱", "Her adım önemli! 🌸"))
        else                    -> random.pickFrom(listOf("Hadi maceraya başlayalım! ✨", "Yeni bir gün, yeni fırsatlar! 🌅", "Bugün ne yapacağız? 🤔"))
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
