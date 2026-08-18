package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.BenimGunlerimTheme

@Preview(name = "RoutineRow — Boş Durum", showBackground = true)
@Composable
private fun RoutineRowEmptyPreview() {
    BenimGunlerimTheme {
        Column(Modifier.padding(16.dp)) {
            RoutineRow(
                title = "Yeni rutin",
                isCompletedToday = false,
                onToggle = {},
                weekHistory = List(7) { false },
            )
        }
    }
}

@Preview(name = "RoutineRow — Dolu/Hedefli Durum", showBackground = true)
@Composable
private fun RoutineRowFilledPreview() {
    BenimGunlerimTheme {
        Column(Modifier.padding(16.dp)) {
            RoutineRow(
                title = "Su iç",
                isCompletedToday = true,
                onToggle = {},
                weekHistory = listOf(true, true, false, true, true, true, true),
                streakCount = 5,
                targetType = "goal",
                targetValue = 8,
                targetUnit = "bardak",
                currentValue = 8f,
            )
        }
    }
}
