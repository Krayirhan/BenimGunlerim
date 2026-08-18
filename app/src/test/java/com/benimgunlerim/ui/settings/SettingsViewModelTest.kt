package com.benimgunlerim.ui.settings

import com.benimgunlerim.analytics.ErrorReporter
import com.benimgunlerim.data.DataExportService
import com.benimgunlerim.data.DataImportService
import com.benimgunlerim.data.DataImportResult
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.LocalDataClearer
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesAccess
import com.benimgunlerim.data.UserPreferencesWriter
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
import com.benimgunlerim.notifications.DailySummarySchedule
import com.benimgunlerim.notifications.MorningPlannerSchedule
import com.benimgunlerim.notifications.NotificationPolicyCache
import com.benimgunlerim.notifications.ReminderRestorer
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Fakes ──────────────────────────────────────────────────────────────

    private inner class FakePrefs(
        private var prefs: UserPreferences = UserPreferences(),
    ) : UserPreferencesAccess {
        override val preferences: Flow<UserPreferences> = flowOf(prefs)
        var notificationMode: String? = null
        var analyticsEnabled: Boolean? = null
        var onboardingReset = false
        var morningPlannerEnabled: Boolean? = null
        var morningPlannerTime: String? = null
        var quietHoursEnabled: Boolean? = null
        var quietHoursStart: String? = null
        var quietHoursEnd: String? = null
        var dailySummaryTime: String? = null
        var lightDayModeEnabled: Boolean? = null
        var lightDayModeDate: String? = null

        override suspend fun setNotificationMode(mode: String) { notificationMode = mode }
        override suspend fun setDailySummaryTime(time: String) { dailySummaryTime = time }
        override suspend fun setAnalyticsEnabled(enabled: Boolean) { analyticsEnabled = enabled }
        override suspend fun resetOnboarding() { onboardingReset = true }
        override suspend fun setMorningPlannerEnabled(enabled: Boolean) { morningPlannerEnabled = enabled }
        override suspend fun setMorningPlannerTime(time: String) { morningPlannerTime = time }
        override suspend fun setQuietHoursEnabled(enabled: Boolean) { quietHoursEnabled = enabled }
        override suspend fun setQuietHoursStart(time: String) { quietHoursStart = time }
        override suspend fun setQuietHoursEnd(time: String) { quietHoursEnd = time }
        override suspend fun setCelebrationEffectsEnabled(enabled: Boolean) { /* tracked via preferences flow in tests if needed */ }
        override suspend fun setLightDayMode(enabled: Boolean, dateStr: String) {
            lightDayModeEnabled = enabled
            lightDayModeDate = dateStr
        }
    }

    private class FakeLocalDataClearer : LocalDataClearer {
        var cleared = false
        override suspend fun clearAllLocalData() { cleared = true }
    }

    private val noopErrorReporter = object : ErrorReporter {
        override fun recordNonFatal(error: Throwable, context: Map<String, String>) = Unit
        override fun setUserProperty(key: String, value: String) = Unit
    }

    private val noopTransactionRunner = object : DatabaseTransactionRunner {
        override suspend fun <T> runInTransaction(block: suspend () -> T): T = block()
    }

    private inner class FakeTaskDao : TaskDao {
        override fun observeByDate(date: String): Flow<List<TaskEntity>> = flowOf(emptyList())
        override fun observeOverdue(today: String): Flow<List<TaskEntity>> = flowOf(emptyList())
        override fun observeRange(from: String, to: String): Flow<List<TaskEntity>> = flowOf(emptyList())
        override suspend fun count(): Int = 0
        override suspend fun getPendingBefore(before: String): List<TaskEntity> = emptyList()
        override suspend fun getPendingRemindersFrom(fromDate: String): List<TaskEntity> = emptyList()
        override suspend fun insert(task: TaskEntity) = Unit
        override suspend fun insertAll(tasks: List<TaskEntity>) = Unit
        override suspend fun update(task: TaskEntity) = Unit
        override suspend fun deleteById(id: String) = Unit
        override suspend fun setCompletionStateById(id: String, state: String, completedAt: Long?) = Unit
        override suspend fun getAll(): List<TaskEntity> = emptyList()
        override suspend fun deleteTemplateTasks() = Unit
        override suspend fun deleteAll() = Unit
    }

    private inner class FakeRoutineDao : RoutineDao {
        override fun observeActive(): Flow<List<RoutineEntity>> = flowOf(emptyList())
        override fun observeArchived(): Flow<List<RoutineEntity>> = flowOf(emptyList())
        override suspend fun getActiveWithReminder(): List<RoutineEntity> = emptyList()
        override suspend fun count(): Int = 0
        override suspend fun insert(routine: RoutineEntity) = Unit
        override suspend fun insertAll(routines: List<RoutineEntity>) = Unit
        override suspend fun update(routine: RoutineEntity) = Unit
        override suspend fun getAll(): List<RoutineEntity> = emptyList()
        override suspend fun deleteByNames(names: List<String>) = Unit
        override suspend fun deleteAll() = Unit
    }

    private inner class FakeSubTaskDao : SubTaskDao {
        override fun observeByTaskId(taskId: String): Flow<List<SubTaskEntity>> = flowOf(emptyList())
        override suspend fun getByTaskId(taskId: String): List<SubTaskEntity> = emptyList()
        override suspend fun getAll(): List<SubTaskEntity> = emptyList()
        override suspend fun insert(subTask: SubTaskEntity) = Unit
        override suspend fun insertAll(subTasks: List<SubTaskEntity>) = Unit
        override suspend fun update(subTask: SubTaskEntity) = Unit
        override suspend fun deleteById(id: String) = Unit
        override suspend fun deleteByTaskId(taskId: String) = Unit
        override suspend fun deleteAll() = Unit
    }

    private inner class FakeCompletionLogDao : CompletionLogDao {
        override fun observeByDate(date: String): Flow<List<CompletionLogEntity>> = flowOf(emptyList())
        override fun observeForEntity(entityType: String, entityId: String): Flow<List<CompletionLogEntity>> = flowOf(emptyList())
        override fun observeAll(): Flow<List<CompletionLogEntity>> = flowOf(emptyList())
        override fun observeBetween(from: String, to: String): Flow<List<CompletionLogEntity>> = flowOf(emptyList())
        override suspend fun getAll(): List<CompletionLogEntity> = emptyList()
        override suspend fun upsert(log: CompletionLogEntity) = Unit
        override suspend fun insertAll(logs: List<CompletionLogEntity>) = Unit
        override suspend fun deleteForDate(entityType: String, entityId: String, date: String) = Unit
        override suspend fun deleteForEntity(entityType: String, entityId: String) = Unit
        override suspend fun deleteAll() = Unit
    }

    private inner class FakeDailyStateDao : DailyStateDao {
        override fun observeByDate(date: String): Flow<DailyStateEntity?> = flowOf(null)
        override suspend fun getByDate(date: String): DailyStateEntity? = null
        override fun observeRecent(limit: Int): Flow<List<DailyStateEntity>> = flowOf(emptyList())
        override suspend fun getAll(): List<DailyStateEntity> = emptyList()
        override suspend fun upsert(state: DailyStateEntity) = Unit
        override suspend fun insertAll(states: List<DailyStateEntity>) = Unit
        override suspend fun deleteAll() = Unit
    }

    private inner class FakeAchievementDao : AchievementDao {
        override fun observeUnlocked(): Flow<List<AchievementEntity>> = flowOf(emptyList())
        override fun observeAll(): Flow<List<AchievementEntity>> = flowOf(emptyList())
        override suspend fun getAll(): List<AchievementEntity> = emptyList()
        override suspend fun getById(id: String): AchievementEntity? = null
        override suspend fun insert(achievement: AchievementEntity) = Unit
        override suspend fun insertAll(achievements: List<AchievementEntity>) = Unit
        override suspend fun unlock(id: String, time: Long): Int = 0
        override suspend fun deleteAll() = Unit
    }

    private inner class FakePreferencesWriter : UserPreferencesWriter {
        override suspend fun replacePreferences(preferences: UserPreferences) = Unit
    }

    private fun makeExportService(json: String? = "{\"version\":1}") = DataExportService(
        taskDao = FakeTaskDao(),
        routineDao = FakeRoutineDao(),
        subTaskDao = FakeSubTaskDao(),
        completionLogDao = FakeCompletionLogDao(),
        dailyStateDao = FakeDailyStateDao(),
        achievementDao = FakeAchievementDao(),
        preferencesRepository = FakePrefs(),
        errorReporter = noopErrorReporter,
    )

    private fun makeImportService(result: DataImportResult?) = DataImportService(
        transactionRunner = noopTransactionRunner,
        taskDao = FakeTaskDao(),
        routineDao = FakeRoutineDao(),
        subTaskDao = FakeSubTaskDao(),
        completionLogDao = FakeCompletionLogDao(),
        dailyStateDao = FakeDailyStateDao(),
        achievementDao = FakeAchievementDao(),
        preferencesWriter = FakePreferencesWriter(),
        errorReporter = if (result == null) {
            object : ErrorReporter {
                override fun recordNonFatal(e: Throwable, context: Map<String, String>) = Unit
                override fun setUserProperty(key: String, value: String) = Unit
            }
        } else noopErrorReporter,
    )

    private fun noopDailySummaryScheduler(): DailySummarySchedule = object : DailySummarySchedule {
        override fun schedule(time: LocalTime) {}
        override fun cancel() {}
    }

    private fun noopMorningPlannerScheduler(): MorningPlannerSchedule = object : MorningPlannerSchedule {
        override fun schedule(time: LocalTime) {}
        override fun cancel() {}
    }

    private fun makeViewModel(
        prefs: FakePrefs = FakePrefs(),
        clearer: FakeLocalDataClearer = FakeLocalDataClearer(),
        exportService: DataExportService = makeExportService(),
        importService: DataImportService = makeImportService(null),
        reminderRestorer: ReminderRestorer = ReminderRestorer { },
        policyCache: NotificationPolicyCache = NotificationPolicyCache { _, _, _, _ -> },
    ) = SettingsViewModel(
        preferencesRepository = prefs,
        localDataClearer = clearer,
        dataExportService = exportService,
        dataImportService = importService,
        dailySummaryScheduler = noopDailySummaryScheduler(),
        morningPlannerScheduler = noopMorningPlannerScheduler(),
        reminderBootstrapper = reminderRestorer,
        reminderPolicy = policyCache,
    )

    // ── Tests: dataOperationMessage state ──────────────────────────────────

    @Test
    fun dataOperationMessage_initialValue_isNull() {
        val vm = makeViewModel()
        assertNull(vm.dataOperationMessage.value)
    }

    @Test
    fun setDataOperationMessage_updatesMessage() {
        val vm = makeViewModel()
        vm.setDataOperationMessage(SettingsEvent.ExportSaved)
        assertEquals(SettingsEvent.ExportSaved, vm.dataOperationMessage.value)
    }

    @Test
    fun clearDataOperationMessage_resetsToNull() {
        val vm = makeViewModel()
        vm.setDataOperationMessage(SettingsEvent.ExportSaved)
        vm.clearDataOperationMessage()
        assertNull(vm.dataOperationMessage.value)
    }

    // ── Tests: clearLocalData ──────────────────────────────────────────────

    @Test
    fun clearLocalData_clearsDataAndSetsMessage() = runTest {
        val prefs = FakePrefs()
        val clearer = FakeLocalDataClearer()
        val vm = makeViewModel(prefs = prefs, clearer = clearer)

        vm.clearLocalData()
        testDispatcher.scheduler.advanceUntilIdle()

        assert(clearer.cleared) { "Expected clearAllLocalData() to be called" }
        assert(prefs.onboardingReset) { "Expected resetOnboarding() to be called" }
        assertEquals(SettingsEvent.DataCleared, vm.dataOperationMessage.value)
    }

    // ── Tests: exportData ──────────────────────────────────────────────────

    @Test
    fun exportData_whenServiceReturnsJson_invokesCallback() = runTest {
        val vm = makeViewModel(exportService = makeExportService())
        var capturedJson: String? = null

        vm.exportData { json -> capturedJson = json }
        testDispatcher.scheduler.advanceUntilIdle()

        assert(capturedJson != null) { "Expected callback to be invoked with JSON" }
        assertNull(vm.dataOperationMessage.value)
    }

    @Test
    fun exportData_whenServiceReturnsNull_setsErrorMessage() = runTest {
        // Create an export service that always fails
        val failingExportService = object : DataExportService(
            taskDao = FakeTaskDao(),
            routineDao = FakeRoutineDao(),
            subTaskDao = FakeSubTaskDao(),
            completionLogDao = FakeCompletionLogDao(),
            dailyStateDao = FakeDailyStateDao(),
            achievementDao = FakeAchievementDao(),
            preferencesRepository = object : com.benimgunlerim.data.UserPreferencesSource {
                override val preferences = flowOf(UserPreferences())
            },
            errorReporter = noopErrorReporter,
        ) {
            override suspend fun exportToJson(): String? = null
        }
        val vm = makeViewModel(exportService = failingExportService)
        var callbackInvoked = false

        vm.exportData { callbackInvoked = true }
        testDispatcher.scheduler.advanceUntilIdle()

        assert(!callbackInvoked) { "Expected callback NOT to be invoked" }
        assertEquals(SettingsEvent.ExportFailed, vm.dataOperationMessage.value)
    }

    @Test
    fun exportDataToFile_whenServiceReturnsJson_emitsSaveEffect() = runTest {
        val vm = makeViewModel(exportService = makeExportService("{\"version\":1}"))
        val effectDeferred = backgroundScope.async { vm.uiEffects.first() }

        vm.exportDataToFile()
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = effectDeferred.await()
        assert(effect is SettingsUiEffect.SaveExportJson) {
            "Expected SaveExportJson effect, got: $effect"
        }
    }

    // ── Tests: importData ──────────────────────────────────────────────────

    @Test
    fun importData_whenImportSucceeds_setsSuccessMessage() = runTest {
        // Build a valid JSON that the real DataImportService will accept
        val validJson = buildMinimalValidImportJson()
        val vm = makeViewModel(importService = makeImportService(
            DataImportResult(tasks = 1, subTasks = 0, routines = 0, completionLogs = 0, dailyStates = 0, achievements = 0)
        ).let {
            // Use the real service which will parse and validate
            DataImportService(
                transactionRunner = noopTransactionRunner,
                taskDao = FakeTaskDao(),
                routineDao = FakeRoutineDao(),
                subTaskDao = FakeSubTaskDao(),
                completionLogDao = FakeCompletionLogDao(),
                dailyStateDao = FakeDailyStateDao(),
                achievementDao = FakeAchievementDao(),
                preferencesWriter = FakePreferencesWriter(),
                errorReporter = noopErrorReporter,
            )
        })

        vm.importData(validJson)
        testDispatcher.scheduler.advanceUntilIdle()

        val event = vm.dataOperationMessage.value
        assert(event is SettingsEvent.ImportSuccess) {
            "Expected ImportSuccess event, got: $event"
        }
    }

    @Test
    fun importData_whenImportFails_setsFailureMessage() = runTest {
        val vm = makeViewModel(importService = DataImportService(
            transactionRunner = noopTransactionRunner,
            taskDao = FakeTaskDao(),
            routineDao = FakeRoutineDao(),
            subTaskDao = FakeSubTaskDao(),
            completionLogDao = FakeCompletionLogDao(),
            dailyStateDao = FakeDailyStateDao(),
            achievementDao = FakeAchievementDao(),
            preferencesWriter = FakePreferencesWriter(),
            errorReporter = noopErrorReporter,
        ))

        vm.importData("{\"version\":999}") // unsupported version
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SettingsEvent.ImportParseFailed, vm.dataOperationMessage.value)
    }

    @Test
    fun requestImportFromFile_emitsRequestImportEffect() = runTest {
        val vm = makeViewModel()
        val effectDeferred = backgroundScope.async { vm.uiEffects.first() }
        testDispatcher.scheduler.runCurrent()

        vm.requestImportFromFile()
        testDispatcher.scheduler.advanceUntilIdle()

        val effect = effectDeferred.await()
        assertEquals(SettingsUiEffect.RequestImportJson, effect)
    }

    @Test
    fun importDataFromFileContent_whenContentIsNull_setsReadFailedMessage() {
        val vm = makeViewModel()

        vm.importDataFromFileContent(null)

        assertEquals(SettingsEvent.ImportReadFailed, vm.dataOperationMessage.value)
    }

    // ── Tests: preferences setter delegation ──────────────────────────────

    @Test
    fun setAnalyticsEnabled_delegatesToPreferencesRepository() = runTest {
        val prefs = FakePrefs()
        val vm = makeViewModel(prefs = prefs)

        vm.setAnalyticsEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(false, prefs.analyticsEnabled)
    }

    @Test
    fun setQuietHoursEnabled_delegatesToPreferencesRepository() = runTest {
        val prefs = FakePrefs()
        val vm = makeViewModel(prefs = prefs)

        vm.setQuietHoursEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(true, prefs.quietHoursEnabled)
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun buildMinimalValidImportJson(): String =
        org.json.JSONObject().apply {
            put("version", com.benimgunlerim.data.DataExportService.EXPORT_VERSION)
            put("tasks", org.json.JSONArray().put(org.json.JSONObject().apply {
                put("id", "t1")
                put("title", "Task")
                put("plannedDate", LocalDate.now().toString())
                put("completionState", "pending")
                put("createdAt", 1L)
                put("updatedAt", 1L)
            }))
            put("subTasks", org.json.JSONArray())
            put("routines", org.json.JSONArray())
            put("completionLogs", org.json.JSONArray())
            put("dailyStates", org.json.JSONArray())
            put("achievements", org.json.JSONArray())
        }.toString()
}
