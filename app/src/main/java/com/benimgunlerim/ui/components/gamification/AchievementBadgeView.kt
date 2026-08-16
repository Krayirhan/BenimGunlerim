package com.benimgunlerim.ui.components.gamification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AchievementCategory {
    TASK,
    ROUTINE,
    CALM,
    LEVEL,
    DAY_CLOSE,
}

fun getAchievementCategory(id: String): AchievementCategory = when {
    id.startsWith("tasks") || id.startsWith("first_task") || id.startsWith("first_plan") || id == "list_cleared" -> AchievementCategory.TASK
    id.startsWith("routines") || id.startsWith("streak") || id.startsWith("first_routine") -> AchievementCategory.ROUTINE
    id.startsWith("calm") -> AchievementCategory.CALM
    id.startsWith("level") || id.startsWith("gold") || id.startsWith("perfect") -> AchievementCategory.LEVEL
    id.startsWith("close") -> AchievementCategory.DAY_CLOSE
    else -> AchievementCategory.TASK
}

@Composable
fun AchievementBadgeView(
    emoji: String,
    category: AchievementCategory,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    fontSize: Int = 36,
) {
    val (bgColor, borderColor, shape) = when (category) {
        AchievementCategory.TASK -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            CircleShape,
        )
        AchievementCategory.ROUTINE -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            RoundedCornerShape(size / 4),
        )
        AchievementCategory.CALM -> Triple(
            Color(0xFFE0F2F1),
            Color(0xFF00796B),
            CircleShape,
        )
        AchievementCategory.LEVEL -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFFFA000),
            CircleShape,
        )
        AchievementCategory.DAY_CLOSE -> Triple(
            Color(0xFFEDE7F6),
            Color(0xFF4527A0),
            RoundedCornerShape(size / 3),
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgColor)
            .border(2.dp, borderColor.copy(alpha = 0.4f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = emoji, fontSize = fontSize.sp)
    }
}
