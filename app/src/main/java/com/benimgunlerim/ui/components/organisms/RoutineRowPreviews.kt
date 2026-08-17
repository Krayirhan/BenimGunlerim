package com.benimgunlerim.ui.components.organisms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.theme.BenimGunlerimTheme

@Preview(name = "RoutineRow — Açık, Boş Durum", showBackground = true)
@Composable
private fun RoutineRowEmptyLightPreview() {
    BenimGunlerimTheme(themeMode = "light") {
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

@Preview(name = "RoutineRow — Açık, Dolu/Hedefli Durum", showBackground = true)
@Composable
private fun RoutineRowFilledLightPreview() {
    BenimGunlerimTheme(themeMode = "light") {
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

@Preview(name = "RoutineRow — Koyu, Boş Durum", showBackground = true)
@Composable
private fun RoutineRowEmptyDarkPreview() {
    BenimGunlerimTheme(themeMode = "dark") {
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

@Preview(name = "RoutineRow — Koyu, Dolu/Hedefli Durum", showBackground = true)
@Composable
private fun RoutineRowFilledDarkPreview() {
    BenimGunlerimTheme(themeMode = "dark") {
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
