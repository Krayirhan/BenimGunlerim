package com.benimgunlerim.domain

import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

data class AchievementDef(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val xpReward: Int = 50,
    val goldReward: Int = 20,
)

val ALL_ACHIEVEMENTS = listOf(
    // First step milestones
    AchievementDef("first_task", "🌱", "İlk Adım", "İlk görevini tamamla", 25, 10),
    AchievementDef("first_routine", "🔄", "İlk Rutin", "İlk rutinini tamamla", 25, 10),
    AchievementDef("first_plan", "📝", "Başlangıç Planı", "İlk görevini oluştur", 20, 10),
    // Streak milestones
    AchievementDef("streak_3", "🔥", "3 Günlük Ritim", "3 günlük seri", 30, 15),
    AchievementDef("streak_7", "⚡", "7 Günlük Ritim", "Bir hafta boyunca ritmini koru", 50, 30),
    AchievementDef("streak_14", "💪", "14 Günlük Ritim", "14 günlük seri", 100, 50),
    AchievementDef("streak_30", "⭐", "30 Günlük Yolculuk", "30 günlük güçlü seri", 200, 100),
    // Task milestones
    AchievementDef("tasks_10", "📋", "Görev Avcısı", "10 görev tamamla", 30, 15),
    AchievementDef("tasks_25", "✨", "Küçük Adımlar", "Toplam 25 görev tamamla", 50, 25),
    AchievementDef("tasks_50", "📝", "Verimli", "50 görev tamamla", 80, 40),
    AchievementDef("tasks_100", "🏆", "Görev Ustası", "100 görev tamamla", 150, 75),
    AchievementDef("tasks_500", "👑", "Görev Kralı", "500 görev tamamla", 300, 150),
    // Routine milestones
    AchievementDef("routines_10", "🔄", "Alışkanlık Başlangıcı", "10 rutin tamamla", 30, 15),
    AchievementDef("routines_50", "💎", "Rutin Ustası", "50 rutin tamamla", 80, 40),
    AchievementDef("routines_100", "🌟", "Disiplin İkonu", "100 rutin tamamla", 150, 75),
    // Perfect day & List clear
    AchievementDef("perfect_1", "✨", "Mükemmel Gün", "İlk %100 gün", 50, 25),
    AchievementDef("list_cleared", "🧹", "Liste Temizlendi", "Bir günde tüm görevlerini tamamla", 40, 20),
    AchievementDef("perfect_5", "🌈", "Altın Hafta", "5 mükemmel gün", 100, 50),
    AchievementDef("perfect_20", "🎯", "Kusursuz", "20 mükemmel gün", 200, 100),
    // Calmness & Mental Health
    AchievementDef("calm_deep_breath", "🫁", "Derin Nefes", "İlk nefes egzersizini tamamla", 25, 10),
    AchievementDef("calm_short_pause", "🧘", "Kısa Duraklama", "3 kez 1 dakikalık reset yap", 40, 20),
    AchievementDef("calm_light_day", "🌿", "Bugünü Hafiflettin", "İlk kez Hafif Gün Modu'nu kullan", 30, 15),
    AchievementDef("calm_brain_dump", "💡", "Kafamı Boşalttım", "İlk Brain Dump notunu göreve dönüştür", 25, 10),
    AchievementDef("calm_gentle_close", "🌙", "Nazik Kapanış", "Günü kapatmadan önce nefes egzersizi yap", 35, 15),
    // Level milestones
    AchievementDef("level_5", "⚡", "Yükselen Yıldız", "Seviye 5'e ulaş", 50, 25),
    AchievementDef("level_10", "🚀", "Gün Mimarı", "Seviye 10'a ulaş", 100, 50),
    AchievementDef("level_20", "🌙", "Ay Yolcusu", "Seviye 20'ye ulaş", 200, 100),
    // Gold milestones
    AchievementDef("gold_100", "🪙", "Biriktirici", "100 altın topla", 30, 0),
    AchievementDef("gold_500", "💰", "Zengin", "500 altın topla", 80, 0),
    AchievementDef("gold_1000", "🏦", "Banker", "1000 altın topla", 150, 0),
    // Day close
    AchievementDef("close_1", "🌙", "Günü Kapattın", "İlk gün sonu kapanışını tamamla", 25, 10),
    AchievementDef("close_10", "📖", "Günlükçü", "10 gün kapat", 60, 30),
    AchievementDef("close_30", "📚", "Kronikçi", "30 gün kapat", 150, 75),
    // Companion
    AchievementDef("companion_happy", "❤️", "Mutlu Dost", "Mutluluk 90'a ulaşsın", 50, 25),
    // Early riser
    AchievementDef("early_task", "🌅", "Erken Kuş", "Sabah 8'den önce görev tamamla", 40, 20),
    // Shop
    AchievementDef("first_buy", "🛍️", "İlk Alışveriş", "Dükkandan bir şey al", 30, 0),
    // Mood
    AchievementDef("mood_5_happy", "😄", "Pozitif Ruh", "5 kez 'harika' seç", 50, 25),
)

private val achievementMap = ALL_ACHIEVEMENTS.associateBy { it.id }

@Singleton
class AchievementTracker @Inject constructor(
    private val achievementDao: AchievementDao,
    private val dateTimeProvider: DateTimeProvider,
) {
    private val _newUnlock = MutableSharedFlow<AchievementDef>(extraBufferCapacity = 5)
    val newUnlock = _newUnlock.asSharedFlow()

    val unlockedAchievements: Flow<List<AchievementDef>> =
        achievementDao.observeUnlocked().map { entities ->
            entities.mapNotNull { achievementMap[it.id] }
        }

    val allProgress: Flow<Map<String, Boolean>> =
        achievementDao.observeAll().map { entities ->
            ALL_ACHIEVEMENTS.associate { def ->
                def.id to (entities.any { it.id == def.id && it.unlockedAt != null })
            }
        }

    suspend fun tryUnlock(id: String): AchievementDef? {
        val def = achievementMap[id] ?: return null
        // Ensure row exists
        achievementDao.insert(AchievementEntity(id = id))
        val updated = achievementDao.unlock(id, dateTimeProvider.currentTimeMillis())
        return if (updated > 0) {
            _newUnlock.tryEmit(def)
            def
        } else null
    }

    suspend fun checkStreak(streak: Int) {
        if (streak >= 3) tryUnlock("streak_3")
        if (streak >= 7) tryUnlock("streak_7")
        if (streak >= 14) tryUnlock("streak_14")
        if (streak >= 30) tryUnlock("streak_30")
    }

    suspend fun checkFirstTask() {
        tryUnlock("first_task")
    }

    suspend fun checkFirstRoutine() {
        tryUnlock("first_routine")
    }

    suspend fun checkFirstPlan() {
        tryUnlock("first_plan")
    }

    suspend fun checkListCleared() {
        tryUnlock("list_cleared")
    }

    suspend fun checkCalmDeepBreath() {
        tryUnlock("calm_deep_breath")
    }

    suspend fun checkCalmShortPause(count: Int) {
        if (count >= 3) tryUnlock("calm_short_pause")
    }

    suspend fun checkCalmLightDay() {
        tryUnlock("calm_light_day")
    }

    suspend fun checkCalmBrainDump() {
        tryUnlock("calm_brain_dump")
    }

    suspend fun checkCalmGentleClose() {
        tryUnlock("calm_gentle_close")
    }

    suspend fun checkTaskCount(count: Int) {
        if (count >= 1) tryUnlock("first_task")
        if (count >= 10) tryUnlock("tasks_10")
        if (count >= 25) tryUnlock("tasks_25")
        if (count >= 50) tryUnlock("tasks_50")
        if (count >= 100) tryUnlock("tasks_100")
        if (count >= 500) tryUnlock("tasks_500")
    }

    suspend fun checkRoutineCount(count: Int) {
        if (count >= 1) tryUnlock("first_routine")
        if (count >= 10) tryUnlock("routines_10")
        if (count >= 50) tryUnlock("routines_50")
        if (count >= 100) tryUnlock("routines_100")
    }

    suspend fun checkPerfectDay(count: Int) {
        if (count >= 1) tryUnlock("perfect_1")
        if (count >= 5) tryUnlock("perfect_5")
        if (count >= 20) tryUnlock("perfect_20")
    }

    suspend fun checkLevel(level: Int) {
        if (level >= 5) tryUnlock("level_5")
        if (level >= 10) tryUnlock("level_10")
        if (level >= 20) tryUnlock("level_20")
    }

    suspend fun checkGold(gold: Int) {
        if (gold >= 100) tryUnlock("gold_100")
        if (gold >= 500) tryUnlock("gold_500")
        if (gold >= 1000) tryUnlock("gold_1000")
    }

    suspend fun checkDayClose(count: Int) {
        if (count >= 1) tryUnlock("close_1")
        if (count >= 10) tryUnlock("close_10")
        if (count >= 30) tryUnlock("close_30")
    }

    suspend fun checkHappiness(happiness: Int) {
        if (happiness >= 90) tryUnlock("companion_happy")
    }
}
