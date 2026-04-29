package com.benimgunlerim.domain.usecase

import com.benimgunlerim.notifications.ReminderRestorer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class RestoreRemindersUseCaseTest {

    private val reminderRestorer: ReminderRestorer = mockk(relaxed = true)
    private lateinit var useCase: RestoreRemindersUseCase

    @Before
    fun setUp() {
        every { reminderRestorer.rescheduleReminders() } returns Unit
        useCase = RestoreRemindersUseCase(reminderRestorer)
    }

    @Test
    fun invoke_callsRescheduleReminders() {
        useCase()

        verify(exactly = 1) { reminderRestorer.rescheduleReminders() }
    }

    @Test
    fun invoke_calledTwice_callsRescheduleRemindersTwice() {
        useCase()
        useCase()

        verify(exactly = 2) { reminderRestorer.rescheduleReminders() }
    }
}
