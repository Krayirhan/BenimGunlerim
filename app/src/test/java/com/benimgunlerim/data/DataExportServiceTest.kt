package com.benimgunlerim.data

import com.benimgunlerim.analytics.ErrorReporter
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.DailyStateDao
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.SubTaskDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataExportServiceTest {

    // ── Test doubles ─────────────────────────────────────────────────────

    private class StubTaskDao(private val records: List<TaskEntity>) : TaskDao {
        override suspend fun getAll() = records
        override fun observeByDate(date: String): Flow<List<TaskEntity>> = error("stub")
        override fun observeOverdue(today: String): Flow<List<TaskEntity>> = error("stub")
        override fun observeRange(from: String, to: String): Flow<List<TaskEntity>> = error("stub")
        override suspend fun count(): Int = records.size
        override suspend fun getPendingBefore(before: String): List<TaskEntity> = error("stub")
        override suspend fun getPendingRemindersFrom(fromDate: String): List<TaskEntity> = error("stub")
        override suspend fun insert(task: TaskEntity) = error("stub")
        override suspend fun insertAll(tasks: List<TaskEntity>) = error("stub")
        override suspend fun update(task: TaskEntity) = error("stub")
        override suspend fun deleteById(id: String) = error("stub")
        override suspend fun setCompletionStateById(id: String, state: String, completedAt: Long?) = error("stub")
        override suspend fun deleteTemplateTasks() = error("stub")
        override suspend fun deleteAll() = error("stub")
    }

    private class StubRoutineDao(private val records: List<RoutineEntity>) : RoutineDao {
        override suspend fun getAll() = records
        override fun observeActive(): Flow<List<RoutineEntity>> = error("stub")
        override suspend fun getActiveWithReminder(): List<RoutineEntity> = error("stub")
        override suspend fun count(): Int = records.size
        override suspend fun insert(routine: RoutineEntity) = error("stub")
        override suspend fun insertAll(routines: List<RoutineEntity>) = error("stub")
        override suspend fun update(routine: RoutineEntity) = error("stub")
        override suspend fun deleteByNames(names: List<String>) = error("stub")
        override suspend fun deleteAll() = error("stub")
    }

    private class StubCompletionLogDao(private val records: List<CompletionLogEntity>) : CompletionLogDao {
        override suspend fun getAll() = records
        override fun observeByDate(date: String): Flow<List<CompletionLogEntity>> = error("stub")
        override fun observeForEntity(entityType: String, entityId: String): Flow<List<CompletionLogEntity>> = error("stub")
        override fun observeAll(): Flow<List<CompletionLogEntity>> = error("stub")
        override fun observeBetween(from: String, to: String): Flow<List<CompletionLogEntity>> = error("stub")
        override suspend fun upsert(log: CompletionLogEntity) = error("stub")
        override suspend fun insertAll(logs: List<CompletionLogEntity>) = error("stub")
        override suspend fun deleteForDate(entityType: String, entityId: String, date: String) = error("stub")
        override suspend fun deleteForEntity(entityType: String, entityId: String) = error("stub")
        override suspend fun deleteAll() = error("stub")
    }

    private class StubDailyStateDao(private val records: List<DailyStateEntity>) : DailyStateDao {
        override suspend fun getAll() = records
        override fun observeByDate(date: String): Flow<DailyStateEntity?> = error("stub")
        override suspend fun getByDate(date: String): DailyStateEntity? = error("stub")
        override fun observeRecent(limit: Int): Flow<List<DailyStateEntity>> = error("stub")
        override suspend fun upsert(state: DailyStateEntity) = error("stub")
        override suspend fun insertAll(states: List<DailyStateEntity>) = error("stub")
        override suspend fun deleteAll() = error("stub")
    }

    private class StubSubTaskDao(private val records: List<SubTaskEntity>) : SubTaskDao {
        override suspend fun getAll() = records
        override fun observeByTaskId(taskId: String): Flow<List<SubTaskEntity>> = error("stub")
        override suspend fun getByTaskId(taskId: String): List<SubTaskEntity> = error("stub")
        override suspend fun insert(subTask: SubTaskEntity) = error("stub")
        override suspend fun insertAll(subTasks: List<SubTaskEntity>) = error("stub")
        override suspend fun update(subTask: SubTaskEntity) = error("stub")
        override suspend fun deleteById(id: String) = error("stub")
        override suspend fun deleteByTaskId(taskId: String) = error("stub")
        override suspend fun deleteAll() = error("stub")
    }

    private class StubAchievementDao(private val records: List<AchievementEntity>) : AchievementDao {
        override suspend fun getAll() = records
        override fun observeUnlocked(): Flow<List<AchievementEntity>> = error("stub")
        override fun observeAll(): Flow<List<AchievementEntity>> = error("stub")
        override suspend fun getById(id: String): AchievementEntity? = error("stub")
        override suspend fun insert(achievement: AchievementEntity) = error("stub")
        override suspend fun insertAll(achievements: List<AchievementEntity>) = error("stub")
        override suspend fun unlock(id: String, time: Long): Int = error("stub")
        override suspend fun deleteAll() = error("stub")
    }

    private class StubPreferencesRepository(private val prefs: UserPreferences) : UserPreferencesSource {
        override val preferences: Flow<UserPreferences> = flowOf(prefs)
    }

    private val noopErrorReporter = object : ErrorReporter {
        override fun recordNonFatal(error: Throwable, context: Map<String, String>) {}
        override fun setUserProperty(key: String, value: String) {}
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun makeService(
        tasks: List<TaskEntity> = emptyList(),
        subTasks: List<SubTaskEntity> = emptyList(),
        routines: List<RoutineEntity> = emptyList(),
        logs: List<CompletionLogEntity> = emptyList(),
        states: List<DailyStateEntity> = emptyList(),
        achievements: List<AchievementEntity> = emptyList(),
        prefs: UserPreferences = UserPreferences(),
    ) = DataExportService(
        taskDao = StubTaskDao(tasks),
        routineDao = StubRoutineDao(routines),
        subTaskDao = StubSubTaskDao(subTasks),
        completionLogDao = StubCompletionLogDao(logs),
        dailyStateDao = StubDailyStateDao(states),
        achievementDao = StubAchievementDao(achievements),
        preferencesRepository = StubPreferencesRepository(prefs),
        errorReporter = noopErrorReporter,
    )

    private fun sampleTask() = TaskEntity(
        id = "task-1",
        title = "Test task",
        note = null,
        plannedDate = "2024-01-01",
        startTime = null,
        endTime = null,
        category = "work",
        color = null,
        completionState = "pending",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000_000L,
        updatedAt = 1_000_000L,
        priority = 1,
        reminderTime = null,
        sortOrder = 0,
        isArchived = false,
        postponedFromDate = null,
    )

    private fun sampleRoutine() = RoutineEntity(
        id = "routine-1",
        name = "Morning run",
        description = null,
        targetDays = "1,2,3,4,5",
        preferredTime = "07:00",
        color = null,
        isArchived = false,
        createdAt = 2_000_000L,
        updatedAt = 2_000_000L,
        targetType = "check",
        targetValue = null,
        targetUnit = null,
        category = null,
        reminderEnabled = true,
        sortOrder = 0,
        bestStreak = 3,
    )

    private fun sampleSubTask() = SubTaskEntity(
        id = "sub-1",
        taskId = "task-1",
        title = "Sub task",
        isCompleted = true,
        sortOrder = 1,
        createdAt = 1_000_001L,
    )

    private fun sampleAchievement() = AchievementEntity(
        id = "ach-1",
        unlockedAt = 1_000_002L,
    )

    private fun sampleLog() = CompletionLogEntity(
        id = "log-1",
        entityType = "routine",
        entityId = "routine-1",
        date = "2024-01-01",
        completedAt = 1_000_000L,
        status = "completed",
        note = null,
        value = null,
        targetValue = null,
        skipReason = null,
    )

    private fun sampleDailyState() = DailyStateEntity(
        date = "2024-01-01",
        mood = "happy",
        energyLevel = 4,
        completionRate = 0.8f,
        note = null,
        reflection = null,
        dailyScore = 80,
    )

    // ── Tests ─────────────────────────────────────────────────────────────

    @Test
    fun exportToJson_returnsValidJsonWithCorrectVersion() = runTest {
        val json = makeService().exportToJson()
        assertNotNull(json)
        val root = JSONObject(json!!)
        assertEquals(DataExportService.EXPORT_VERSION, root.getInt("version"))
    }

    @Test
    fun exportToJson_containsExportedAtIsoString() = runTest {
        val json = makeService().exportToJson()
        val root = JSONObject(json!!)
        val exportedAt = root.getString("exportedAt")
        assertTrue("exportedAt should be non-empty ISO string", exportedAt.isNotBlank())
        assertTrue("exportedAt should contain 'T'", exportedAt.contains('T'))
    }

    @Test
    fun exportToJson_tasksArrayHasCorrectCount() = runTest {
        val json = makeService(tasks = listOf(sampleTask(), sampleTask().copy(id = "task-2"))).exportToJson()
        val root = JSONObject(json!!)
        assertEquals(2, root.getJSONArray("tasks").length())
    }

    @Test
    fun exportToJson_taskObjectHasRequiredFields() = runTest {
        val json = makeService(tasks = listOf(sampleTask())).exportToJson()
        val task = JSONObject(json!!).getJSONArray("tasks").getJSONObject(0)
        assertEquals("task-1", task.getString("id"))
        assertEquals("Test task", task.getString("title"))
        assertEquals("2024-01-01", task.getString("plannedDate"))
        assertEquals("pending", task.getString("completionState"))
        assertEquals(1, task.getInt("priority"))
    }

    @Test
    fun exportToJson_subTaskObjectHasRequiredFields() = runTest {
        val json = makeService(subTasks = listOf(sampleSubTask())).exportToJson()
        val subTask = JSONObject(json!!).getJSONArray("subTasks").getJSONObject(0)
        assertEquals("sub-1", subTask.getString("id"))
        assertEquals("task-1", subTask.getString("taskId"))
        assertEquals("Sub task", subTask.getString("title"))
        assertEquals(true, subTask.getBoolean("isCompleted"))
    }

    @Test
    fun exportToJson_routineObjectHasRequiredFields() = runTest {
        val json = makeService(routines = listOf(sampleRoutine())).exportToJson()
        val routine = JSONObject(json!!).getJSONArray("routines").getJSONObject(0)
        assertEquals("routine-1", routine.getString("id"))
        assertEquals("Morning run", routine.getString("name"))
        assertEquals("check", routine.getString("targetType"))
        assertEquals(3, routine.getInt("bestStreak"))
    }

    @Test
    fun exportToJson_completionLogObjectHasRequiredFields() = runTest {
        val json = makeService(logs = listOf(sampleLog())).exportToJson()
        val log = JSONObject(json!!).getJSONArray("completionLogs").getJSONObject(0)
        assertEquals("log-1", log.getString("id"))
        assertEquals("routine", log.getString("entityType"))
        assertEquals("completed", log.getString("status"))
    }

    @Test
    fun exportToJson_dailyStateObjectHasRequiredFields() = runTest {
        val json = makeService(states = listOf(sampleDailyState())).exportToJson()
        val state = JSONObject(json!!).getJSONArray("dailyStates").getJSONObject(0)
        assertEquals("2024-01-01", state.getString("date"))
        assertEquals("happy", state.getString("mood"))
        assertEquals(80, state.getInt("dailyScore"))
    }

    @Test
    fun exportToJson_achievementObjectHasRequiredFields() = runTest {
        val json = makeService(achievements = listOf(sampleAchievement())).exportToJson()
        val achievement = JSONObject(json!!).getJSONArray("achievements").getJSONObject(0)
        assertEquals("ach-1", achievement.getString("id"))
        assertEquals(1_000_002L, achievement.getLong("unlockedAt"))
    }

    @Test
    fun exportToJson_preferencesHasExpectedSettings() = runTest {
        val customPrefs = UserPreferences(
            themeMode = "dark",
            totalXp = 500,
            gold = 120,
            companionType = "dog",
            companionName = "Rex",
        )
        val json = makeService(prefs = customPrefs).exportToJson()
        val prefs = JSONObject(json!!).getJSONObject("preferences")
        assertEquals("dark", prefs.getString("themeMode"))
        assertEquals(500, prefs.getInt("totalXp"))
        assertEquals(120, prefs.getInt("gold"))
        assertEquals("dog", prefs.getString("companionType"))
        assertEquals("Rex", prefs.getString("companionName"))
    }

    @Test
    fun exportToJson_returnsNullOnDaoError() = runTest {
        val errors = mutableListOf<Throwable>()
        val crashingRepo = object : UserPreferencesSource {
            override val preferences: Flow<UserPreferences>
                get() = throw RuntimeException("simulated DAO crash")
        }
        val service = DataExportService(
            taskDao = StubTaskDao(emptyList()),
            routineDao = StubRoutineDao(emptyList()),
            subTaskDao = StubSubTaskDao(emptyList()),
            completionLogDao = StubCompletionLogDao(emptyList()),
            dailyStateDao = StubDailyStateDao(emptyList()),
            achievementDao = StubAchievementDao(emptyList()),
            preferencesRepository = crashingRepo,
            errorReporter = object : ErrorReporter {
                override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
                    errors.add(error)
                }
                override fun setUserProperty(key: String, value: String) {}
            },
        )
        val result = service.exportToJson()
        assertTrue("should return null on error", result == null)
        assertEquals("error should be reported", 1, errors.size)
    }
}
