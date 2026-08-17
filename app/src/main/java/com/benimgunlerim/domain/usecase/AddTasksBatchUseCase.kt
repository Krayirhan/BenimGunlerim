package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import java.time.LocalDate
import javax.inject.Inject

/**
 * Adds several tasks (e.g. from Brain Dump) as a single atomic operation.
 *
 * Runs all inserts inside one DB transaction so a failure partway through
 * (e.g. 3 of 10 titles inserted, then an error) can't leave a half-added
 * batch behind — either every title becomes a task, or none do.
 */
class AddTasksBatchUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val transactionRunner: DatabaseTransactionRunner,
    private val dateTimeProvider: DateTimeProvider,
) {
    suspend operator fun invoke(
        titles: List<String>,
        date: LocalDate = dateTimeProvider.today(),
        priority: Int = 2,
    ): List<TaskEntity> {
        val cleanTitles = titles.map { it.trim() }.filter { it.isNotBlank() }
        if (cleanTitles.isEmpty()) return emptyList()
        return transactionRunner.runInTransaction {
            cleanTitles.map { title ->
                taskRepository.addTask(title = title, date = date, priority = priority)
            }
        }
    }
}
