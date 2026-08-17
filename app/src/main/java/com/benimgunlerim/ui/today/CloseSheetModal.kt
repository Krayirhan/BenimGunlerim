package com.benimgunlerim.ui.today

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CloseSheetModal(
    completedCount: Int,
    totalCount: Int,
    overdueCount: Int,
    onDismiss: () -> Unit,
    onSave: (mood: Int, energy: Int, note: String, bestMoment: String, challenge: String, tomorrowIntention: String, carryTasks: Boolean) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        CloseDaySheet(
            completedCount = completedCount,
            totalCount = totalCount,
            progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f,
            overdueCount = overdueCount,
            onSave = onSave,
        )
    }
}
