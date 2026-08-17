@file:Suppress(
    "LongParameterList",
    "LongMethod",
    "CyclomaticComplexMethod",
    "MagicNumber",
    "MaxLineLength",
)

package com.benimgunlerim.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.benimgunlerim.R
import com.benimgunlerim.ui.theme.CandyPrimary
import com.benimgunlerim.ui.theme.CandySecondary
import com.benimgunlerim.ui.theme.CompletedGreen
import com.benimgunlerim.ui.theme.StreakCoral

@Composable
internal fun CloseDaySheet(
    completedCount: Int,
    totalCount: Int,
    progress: Float,
    overdueCount: Int,
    onSave: (mood: Int, energy: Int, note: String, bestMoment: String, challenge: String, tomorrowIntention: String, carryTasks: Boolean) -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(0) }
    var mood by rememberSaveable { mutableStateOf(3) }
    var energy by rememberSaveable { mutableStateOf(3) }
    var note by rememberSaveable { mutableStateOf("") }
    var bestMoment by rememberSaveable { mutableStateOf("") }
    var challenge by rememberSaveable { mutableStateOf("") }
    var tomorrowIntention by rememberSaveable { mutableStateOf("") }
    var carryTasks by rememberSaveable { mutableStateOf(overdueCount > 0) }

    val moodLabels = listOf(
        stringResource(R.string.today_close_step1_mood_very_bad),
        stringResource(R.string.today_close_step1_mood_bad),
        stringResource(R.string.today_close_step1_mood_normal),
        stringResource(R.string.today_close_step1_mood_good),
        stringResource(R.string.today_close_step1_mood_great),
    )
    val moodColors = listOf(StreakCoral, StreakCoral.copy(.65f), CandySecondary, CandyPrimary, CompletedGreen)

    val closeScroll = rememberScrollState()
    val focusManager = LocalFocusManager.current
    Column(
        Modifier
            .verticalScroll(closeScroll)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                Box(
                    Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (i <= step) CandySecondary else MaterialTheme.colorScheme.surfaceVariant),
                )
            }
        }

        when (step) {
            0 -> CloseDayStep0Summary(completedCount, totalCount, progress, overdueCount)
            1 -> CloseDayStep1MoodEnergy(
                mood = mood,
                onMoodChange = { mood = it },
                energy = energy,
                onEnergyChange = { energy = it },
                moodLabels = moodLabels,
                moodColors = moodColors,
            )
            2 -> CloseDayStep2Reflection(
                bestMoment = bestMoment,
                onBestMomentChange = { bestMoment = it },
                challenge = challenge,
                onChallengeChange = { challenge = it },
                note = note,
                onNoteChange = { note = it },
                onNoteDone = { focusManager.clearFocus() },
            )
            3 -> CloseDayStep3IntentionAndCarry(
                tomorrowIntention = tomorrowIntention,
                onTomorrowIntentionChange = { tomorrowIntention = it },
                onIntentionDone = { focusManager.clearFocus() },
                overdueCount = overdueCount,
                carryTasks = carryTasks,
                onCarryTasksToggle = { carryTasks = !carryTasks },
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (step > 0) {
                OutlinedSmallButton(stringResource(R.string.today_close_back_btn), Modifier.weight(1f)) { step-- }
            }
            if (step < 3) {
                Button(onClick = { step++ }, modifier = Modifier.weight(1f).height(50.dp), shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(CandySecondary, Color.White)) {
                    Text(stringResource(R.string.today_close_next_btn))
                }
            } else {
                Button(
                    onClick = { onSave(mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(CandySecondary, Color.White),
                ) { Text(stringResource(R.string.today_close_save_btn)) }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
