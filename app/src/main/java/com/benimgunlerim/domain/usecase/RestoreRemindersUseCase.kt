package com.benimgunlerim.domain.usecase

import com.benimgunlerim.notifications.ReminderRestorer
import javax.inject.Inject

/**
 * Use-case wrapper around [ReminderRestorer] so that reminder restoration can
 * be invoked through the domain use-case layer (e.g. from a ViewModel or a
 * background worker) rather than depending on the bootstrapper directly.
 */
class RestoreRemindersUseCase @Inject constructor(
    private val reminderRestorer: ReminderRestorer,
) {
    operator fun invoke() = reminderRestorer.rescheduleReminders()
}
