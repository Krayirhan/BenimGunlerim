package com.benimgunlerim.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataClearerImpl @Inject constructor(
    private val completionLogRepository: CompletionLogRepository,
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val transactionRunner: DatabaseTransactionRunner,
) : LocalDataClearer {
    override suspend fun clearAllLocalData() {
        transactionRunner.runInTransaction {
            completionLogRepository.deleteAll()
            taskRepository.deleteAll()
            taskRepository.deleteAllSubTasks()
            routineRepository.deleteAll()
            dailyStateRepository.deleteAll()
        }
    }
}
