package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.model.TaskCompletionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddTasksBatchUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val transactionRunner: DatabaseTransactionRunner = mockk()
    private val dateTimeProvider: DateTimeProvider = mockk()

    private val useCase = AddTasksBatchUseCase(taskRepository, transactionRunner, dateTimeProvider)

    private val fixedDate = LocalDate.of(2025, 1, 15)

    @Before
    fun setUp() {
        every { dateTimeProvider.today() } returns fixedDate
        coEvery { transactionRunner.runInTransaction<List<TaskEntity>>(any()) } coAnswers {
            val block: suspend () -> List<TaskEntity> = firstArg()
            block()
        }
        coEvery { taskRepository.addTask(any(), any(), any(), any(), any(), any(), any()) } answers {
            val title = firstArg<String>()
            task(title)
        }
    }

    @Test
    fun invoke_emptyList_doesNotOpenTransaction() = runTest {
        val result = useCase(emptyList())

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { transactionRunner.runInTransaction<List<TaskEntity>>(any()) }
    }

    @Test
    fun invoke_blankOnlyTitles_doesNotOpenTransaction() = runTest {
        val result = useCase(listOf("  ", "\n", ""))

        assertTrue(result.isEmpty())
        coVerify(exactly = 0) { transactionRunner.runInTransaction<List<TaskEntity>>(any()) }
    }

    @Test
    fun invoke_multipleTitles_insertsAllInsideOneTransaction() = runTest {
        val titles = listOf("Su iç", "Kitap oku", "Yürüyüş yap")

        val result = useCase(titles)

        assertEquals(3, result.size)
        coVerify(exactly = 1) { transactionRunner.runInTransaction<List<TaskEntity>>(any()) }
        titles.forEach { title ->
            coVerify { taskRepository.addTask(title = title, date = fixedDate, priority = 2, note = null, startTime = null, category = null, reminderTime = null) }
        }
    }

    @Test
    fun invoke_transactionFails_propagatesException_noPartialInsertObservable() = runTest {
        coEvery { transactionRunner.runInTransaction<List<TaskEntity>>(any()) } throws RuntimeException("db error")

        var thrown = false
        try {
            useCase(listOf("Su iç", "Kitap oku"))
        } catch (e: RuntimeException) {
            thrown = true
        }

        assertTrue(thrown)
    }

    @Test
    fun invoke_usesProvidedPriority() = runTest {
        useCase(listOf("Su iç"), priority = 1)

        coVerify { taskRepository.addTask(title = "Su iç", date = fixedDate, priority = 1, note = null, startTime = null, category = null, reminderTime = null) }
    }

    private fun task(title: String) = TaskEntity(
        id = title,
        title = title,
        note = null,
        plannedDate = fixedDate.toString(),
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = TaskCompletionState.PENDING.value,
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )
}
