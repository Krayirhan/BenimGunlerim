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
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DataImportServiceTest {
    private val noopErrorReporter = object : ErrorReporter {
        override fun recordNonFatal(error: Throwable, context: Map<String, String>) = Unit
        override fun setUserProperty(key: String, value: String) = Unit
    }

    private class FakeTaskDao : TaskDao {
        val records = mutableListOf<TaskEntity>()
        override fun observeByDate(date: String): Flow<List<TaskEntity>> = flowOf(records.filter { it.plannedDate == date })
        override fun observeOverdue(today: String): Flow<List<TaskEntity>> = flowOf(emptyList())
        override fun observeRange(from: String, to: String): Flow<List<TaskEntity>> = flowOf(emptyList())
        override suspend fun count(): Int = records.size
        override suspend fun getPendingBefore(before: String): List<TaskEntity> = emptyList()
        override suspend fun getPendingRemindersFrom(fromDate: String): List<TaskEntity> = emptyList()
        override suspend fun insert(task: TaskEntity) { records.add(task) }
        override suspend fun insertAll(tasks: List<TaskEntity>) { records.addAll(tasks) }
        override suspend fun update(task: TaskEntity) = Unit
        override suspend fun deleteById(id: String) { records.removeAll { it.id == id } }
        override suspend fun setCompletionStateById(id: String, state: String, completedAt: Long?) = Unit
        override suspend fun getAll(): List<TaskEntity> = records.toList()
        override suspend fun deleteTemplateTasks() = Unit
        override suspend fun deleteAll() { records.clear() }
    }

    private class FakeRoutineDao : RoutineDao {
        val records = mutableListOf<RoutineEntity>()
        override fun observeActive(): Flow<List<RoutineEntity>> = flowOf(records.filter { !it.isArchived })
        override suspend fun getActiveWithReminder(): List<RoutineEntity> = records.filter { !it.isArchived && it.preferredTime != null }
        override suspend fun count(): Int = records.size
        override suspend fun insert(routine: RoutineEntity) { records.add(routine) }
        override suspend fun insertAll(routines: List<RoutineEntity>) { records.addAll(routines) }
        override suspend fun update(routine: RoutineEntity) = Unit
        override suspend fun getAll(): List<RoutineEntity> = records.toList()
        override suspend fun deleteByNames(names: List<String>) { records.removeAll { it.name in names } }
        override suspend fun deleteAll() { records.clear() }
    }

    private class FakeSubTaskDao : SubTaskDao {
        val records = mutableListOf<SubTaskEntity>()
        override fun observeByTaskId(taskId: String): Flow<List<SubTaskEntity>> = flowOf(records.filter { it.taskId == taskId })
        override suspend fun getAll(): List<SubTaskEntity> = records.toList()
        override suspend fun insert(subTask: SubTaskEntity) { records.add(subTask) }
        override suspend fun insertAll(subTasks: List<SubTaskEntity>) { records.addAll(subTasks) }
        override suspend fun update(subTask: SubTaskEntity) = Unit
        override suspend fun deleteById(id: String) { records.removeAll { it.id == id } }
        override suspend fun deleteByTaskId(taskId: String) { records.removeAll { it.taskId == taskId } }
        override suspend fun deleteAll() { records.clear() }
    }

    private class FakeCompletionLogDao : CompletionLogDao {
        val records = mutableListOf<CompletionLogEntity>()
        override fun observeByDate(date: String): Flow<List<CompletionLogEntity>> = flowOf(records.filter { it.date == date })
        override fun observeForEntity(entityType: String, entityId: String): Flow<List<CompletionLogEntity>> = flowOf(emptyList())
        override fun observeAll(): Flow<List<CompletionLogEntity>> = flowOf(records)
        override fun observeBetween(from: String, to: String): Flow<List<CompletionLogEntity>> = flowOf(emptyList())
        override suspend fun getAll(): List<CompletionLogEntity> = records.toList()
        override suspend fun upsert(log: CompletionLogEntity) { records.add(log) }
        override suspend fun insertAll(logs: List<CompletionLogEntity>) { records.addAll(logs) }
        override suspend fun deleteForDate(entityType: String, entityId: String, date: String) = Unit
        override suspend fun deleteForEntity(entityType: String, entityId: String) = Unit
        override suspend fun deleteAll() { records.clear() }
    }

    private class FakeDailyStateDao : DailyStateDao {
        val records = mutableListOf<DailyStateEntity>()
        override fun observeByDate(date: String): Flow<DailyStateEntity?> = flowOf(records.firstOrNull { it.date == date })
        override suspend fun getByDate(date: String): DailyStateEntity? = records.firstOrNull { it.date == date }
        override fun observeRecent(limit: Int): Flow<List<DailyStateEntity>> = flowOf(records.take(limit))
        override suspend fun getAll(): List<DailyStateEntity> = records.toList()
        override suspend fun upsert(state: DailyStateEntity) { records.add(state) }
        override suspend fun insertAll(states: List<DailyStateEntity>) { records.addAll(states) }
        override suspend fun deleteAll() { records.clear() }
    }

    private class FakeAchievementDao : AchievementDao {
        val records = mutableListOf<AchievementEntity>()
        override fun observeUnlocked(): Flow<List<AchievementEntity>> = flowOf(records.filter { it.unlockedAt != null })
        override fun observeAll(): Flow<List<AchievementEntity>> = flowOf(records)
        override suspend fun getAll(): List<AchievementEntity> = records.toList()
        override suspend fun getById(id: String): AchievementEntity? = records.firstOrNull { it.id == id }
        override suspend fun insert(achievement: AchievementEntity) { records.add(achievement) }
        override suspend fun insertAll(achievements: List<AchievementEntity>) { records.addAll(achievements) }
        override suspend fun unlock(id: String, time: Long): Int = 0
        override suspend fun deleteAll() { records.clear() }
    }

    private class FakePreferencesStore(
        private val sourcePreferences: UserPreferences = UserPreferences(),
    ) : UserPreferencesSource, UserPreferencesWriter {
        var last: UserPreferences? = null
        override val preferences: Flow<UserPreferences> = flowOf(sourcePreferences)
        override suspend fun replacePreferences(preferences: UserPreferences) {
            last = preferences
        }
    }

    private val transactionRunner = object : DatabaseTransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }

    @Test
    fun importFromJson_replacesDataAndPreferences() = runTest {
        val taskDao = FakeTaskDao().apply {
            records.add(sampleTask("old-task"))
        }
        val routineDao = FakeRoutineDao()
        val subTaskDao = FakeSubTaskDao()
        val logDao = FakeCompletionLogDao()
        val stateDao = FakeDailyStateDao()
        val achievementDao = FakeAchievementDao()
        val prefsWriter = FakePreferencesStore()
        val service = makeService(taskDao, routineDao, subTaskDao, logDao, stateDao, achievementDao, prefsWriter)

        val result = service.importFromJson(sampleImportJson())

        assertNotNull(result)
        assertEquals(1, result!!.tasks)
        assertEquals(1, result.subTasks)
        assertEquals(1, result.routines)
        assertEquals(1, result.completionLogs)
        assertEquals(1, result.dailyStates)
        assertEquals(1, result.achievements)
        assertEquals(listOf("task-1"), taskDao.records.map { it.id })
        assertEquals("sub-1", subTaskDao.records.single().id)
        assertEquals("routine-1", routineDao.records.single().id)
        assertEquals("ach-1", achievementDao.records.single().id)
        assertEquals("dark", prefsWriter.last?.themeMode)
        assertEquals(42, prefsWriter.last?.totalXp)
    }

    @Test
    fun exportedJson_canBeImportedIntoEmptyStore() = runTest {
        val sourceTaskDao = FakeTaskDao().apply { records.add(sampleTask("task-1")) }
        val sourceRoutineDao = FakeRoutineDao().apply {
            records.add(
                RoutineEntity(
                    id = "routine-1",
                    name = "Routine",
                    description = null,
                    targetDays = "MONDAY",
                    preferredTime = "08:00",
                    color = null,
                    isArchived = false,
                    createdAt = 1L,
                    updatedAt = 2L,
                ),
            )
        }
        val sourceSubTaskDao = FakeSubTaskDao().apply {
            records.add(SubTaskEntity("sub-1", "task-1", "Sub", isCompleted = false, sortOrder = 0, createdAt = 3L))
        }
        val sourceLogDao = FakeCompletionLogDao().apply {
            records.add(CompletionLogEntity("log-1", "task", "task-1", "2026-04-13", 4L, "completed", null))
        }
        val sourceStateDao = FakeDailyStateDao().apply {
            records.add(DailyStateEntity("2026-04-13", "happy", 4, 1f, null, null, 100))
        }
        val sourceAchievementDao = FakeAchievementDao().apply {
            records.add(AchievementEntity("ach-1", 5L))
        }
        val sourcePrefs = FakePreferencesStore(UserPreferences(themeMode = "dark", totalXp = 99))
        val exportService = DataExportService(
            taskDao = sourceTaskDao,
            routineDao = sourceRoutineDao,
            subTaskDao = sourceSubTaskDao,
            completionLogDao = sourceLogDao,
            dailyStateDao = sourceStateDao,
            achievementDao = sourceAchievementDao,
            preferencesRepository = sourcePrefs,
            errorReporter = noopErrorReporter,
        )
        val json = exportService.exportToJson()
        assertNotNull(json)

        val targetTaskDao = FakeTaskDao()
        val targetRoutineDao = FakeRoutineDao()
        val targetSubTaskDao = FakeSubTaskDao()
        val targetLogDao = FakeCompletionLogDao()
        val targetStateDao = FakeDailyStateDao()
        val targetAchievementDao = FakeAchievementDao()
        val targetPrefs = FakePreferencesStore()
        val importService = makeService(
            taskDao = targetTaskDao,
            routineDao = targetRoutineDao,
            subTaskDao = targetSubTaskDao,
            logDao = targetLogDao,
            stateDao = targetStateDao,
            achievementDao = targetAchievementDao,
            prefsWriter = targetPrefs,
        )

        val result = importService.importFromJson(json!!)

        assertNotNull(result)
        assertEquals(sourceTaskDao.records.map { it.id }, targetTaskDao.records.map { it.id })
        assertEquals(sourceRoutineDao.records.map { it.id }, targetRoutineDao.records.map { it.id })
        assertEquals(sourceSubTaskDao.records.map { it.id }, targetSubTaskDao.records.map { it.id })
        assertEquals(sourceLogDao.records.map { it.id }, targetLogDao.records.map { it.id })
        assertEquals(sourceStateDao.records.map { it.date }, targetStateDao.records.map { it.date })
        assertEquals(sourceAchievementDao.records.map { it.id }, targetAchievementDao.records.map { it.id })
        assertEquals("dark", targetPrefs.last?.themeMode)
        assertEquals(99, targetPrefs.last?.totalXp)
    }

    @Test
    fun importFromJson_returnsNullAndReportsUnsupportedVersion() = runTest {
        val errors = mutableListOf<Throwable>()
        val service = makeService(errorReporter = object : ErrorReporter {
            override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
                errors.add(error)
            }
            override fun setUserProperty(key: String, value: String) = Unit
        })

        val result = service.importFromJson(JSONObject().put("version", 999).toString())

        assertNull(result)
        assertEquals(1, errors.size)
    }

    @Test
    fun importFromJson_returnsNullOnInvalidTaskDate() = runTest {
        val errors = mutableListOf<Throwable>()
        val service = makeService(errorReporter = object : ErrorReporter {
            override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
                errors.add(error)
            }
            override fun setUserProperty(key: String, value: String) = Unit
        })

        val invalidJson = JSONObject(sampleImportJson()).apply {
            put("tasks", JSONArray().put(JSONObject().apply {
                put("id", "task-1")
                put("title", "Task")
                put("plannedDate", "13-04-2026")
                put("completionState", "pending")
                put("createdAt", 1L)
                put("updatedAt", 2L)
            }))
        }.toString()

        val result = service.importFromJson(invalidJson)

        assertNull(result)
        assertEquals(1, errors.size)
    }

    @Test
    fun importFromJson_returnsNullOnDanglingSubTaskReference() = runTest {
        val errors = mutableListOf<Throwable>()
        val service = makeService(errorReporter = object : ErrorReporter {
            override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
                errors.add(error)
            }
            override fun setUserProperty(key: String, value: String) = Unit
        })

        val invalidJson = JSONObject(sampleImportJson()).apply {
            put("subTasks", JSONArray().put(JSONObject().apply {
                put("id", "sub-1")
                put("taskId", "missing-task")
                put("title", "Sub")
                put("createdAt", 3L)
            }))
        }.toString()

        val result = service.importFromJson(invalidJson)

        assertNull(result)
        assertEquals(1, errors.size)
    }

    @Test
    fun previewImport_returnsCountsWithoutWritingToStore() = runTest {
        val taskDao = FakeTaskDao().apply { records.add(sampleTask("old-task")) }
        val prefsWriter = FakePreferencesStore()
        val service = makeService(taskDao = taskDao, prefsWriter = prefsWriter)

        val preview = service.previewImport(sampleImportJson())

        assertNotNull(preview)
        assertEquals(1, preview!!.tasks)
        assertEquals(1, preview.subTasks)
        assertEquals(1, preview.routines)
        assertEquals(1, preview.completionLogs)
        assertEquals(1, preview.dailyStates)
        assertEquals(1, preview.achievements)
        // Dry-run: existing store must remain untouched.
        assertEquals(listOf("old-task"), taskDao.records.map { it.id })
        assertNull(prefsWriter.last)
    }

    @Test
    fun previewImport_returnsNullOnInvalidPayload() = runTest {
        val errors = mutableListOf<Throwable>()
        val service = makeService(errorReporter = object : ErrorReporter {
            override fun recordNonFatal(error: Throwable, context: Map<String, String>) {
                errors.add(error)
            }
            override fun setUserProperty(key: String, value: String) = Unit
        })

        val invalidJson = JSONObject(sampleImportJson()).apply {
            put("completionLogs", JSONArray().put(JSONObject().apply {
                put("id", "log-1")
                put("entityType", "task")
                put("entityId", "missing-task")
                put("date", "2026-04-13")
                put("status", "completed")
            }))
        }.toString()

        val preview = service.previewImport(invalidJson)

        assertNull(preview)
        assertEquals(1, errors.size)
    }

    private fun makeService(
        taskDao: FakeTaskDao = FakeTaskDao(),
        routineDao: FakeRoutineDao = FakeRoutineDao(),
        subTaskDao: FakeSubTaskDao = FakeSubTaskDao(),
        logDao: FakeCompletionLogDao = FakeCompletionLogDao(),
        stateDao: FakeDailyStateDao = FakeDailyStateDao(),
        achievementDao: FakeAchievementDao = FakeAchievementDao(),
        prefsWriter: FakePreferencesStore = FakePreferencesStore(),
        errorReporter: ErrorReporter = object : ErrorReporter {
            override fun recordNonFatal(error: Throwable, context: Map<String, String>) = Unit
            override fun setUserProperty(key: String, value: String) = Unit
        },
    ) = DataImportService(
        transactionRunner = transactionRunner,
        taskDao = taskDao,
        routineDao = routineDao,
        subTaskDao = subTaskDao,
        completionLogDao = logDao,
        dailyStateDao = stateDao,
        achievementDao = achievementDao,
        preferencesWriter = prefsWriter,
        errorReporter = errorReporter,
    )

    private fun sampleImportJson(): String = JSONObject().apply {
        put("version", DataExportService.EXPORT_VERSION)
        put("tasks", JSONArray().put(JSONObject().apply {
            put("id", "task-1")
            put("title", "Task")
            put("plannedDate", "2026-04-13")
            put("completionState", "pending")
            put("createdAt", 1L)
            put("updatedAt", 2L)
        }))
        put("subTasks", JSONArray().put(JSONObject().apply {
            put("id", "sub-1")
            put("taskId", "task-1")
            put("title", "Sub")
            put("createdAt", 3L)
        }))
        put("routines", JSONArray().put(JSONObject().apply {
            put("id", "routine-1")
            put("name", "Routine")
            put("targetDays", "MONDAY")
            put("isArchived", false)
            put("createdAt", 4L)
            put("updatedAt", 5L)
        }))
        put("completionLogs", JSONArray().put(JSONObject().apply {
            put("id", "log-1")
            put("entityType", "task")
            put("entityId", "task-1")
            put("date", "2026-04-13")
            put("status", "completed")
        }))
        put("dailyStates", JSONArray().put(JSONObject().apply {
            put("date", "2026-04-13")
            put("completionRate", 1.0)
            put("dailyScore", 100)
        }))
        put("achievements", JSONArray().put(JSONObject().apply {
            put("id", "ach-1")
            put("unlockedAt", 6L)
        }))
        put("preferences", JSONObject().apply {
            put("themeMode", "dark")
            put("totalXp", 42)
        })
    }.toString()

    private fun sampleTask(id: String) = TaskEntity(
        id = id,
        title = "Old",
        note = null,
        plannedDate = "2026-04-12",
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "pending",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 0L,
        updatedAt = 0L,
    )
}
