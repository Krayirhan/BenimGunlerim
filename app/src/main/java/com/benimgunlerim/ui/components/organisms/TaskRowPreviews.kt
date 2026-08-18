package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.BenimGunlerimTheme

@Preview(name = "TaskRow — Boş Durum", showBackground = true)
@Composable
private fun TaskRowEmptyPreview() {
    BenimGunlerimTheme {
        Column(Modifier.padding(16.dp)) {
            TaskRow(
                title = "Yeni görev başlığı",
                isCompleted = false,
                onToggleComplete = {},
                onDelete = {},
            )
        }
    }
}

@Preview(name = "TaskRow — Dolu Durum", showBackground = true)
@Composable
private fun TaskRowFilledPreview() {
    BenimGunlerimTheme {
        Column(Modifier.padding(16.dp)) {
            TaskRow(
                title = "Sabah koşusu yap",
                isCompleted = true,
                onToggleComplete = {},
                onDelete = {},
                dueTime = "07:30",
                priority = 1,
                category = "Sağlık",
            )
        }
    }
}
