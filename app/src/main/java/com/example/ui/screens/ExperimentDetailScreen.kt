package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyCheck
import com.example.data.model.EnergyExperiment
import com.example.data.model.ExperimentStatus
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentDetailScreen(
    experiment: EnergyExperiment,
    onBack: () -> Unit,
    onStartExperiment: () -> Unit,
    onLogDailyCheck: (day: Int, kwh: Double?, notes: String) -> Boolean
) {
    var checkDialogDay by remember { mutableStateOf<Int?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("exp_detail_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
            }
            Text(
                text = "Experiment Overview",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
        }

        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = when (experiment.status) {
                            ExperimentStatus.ACTIVE -> FreshGreen
                            ExperimentStatus.COMPLETED -> EmeraldGreen
                            ExperimentStatus.NOT_STARTED -> LightSage
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = when (experiment.status) {
                                ExperimentStatus.ACTIVE -> "Active: ${experiment.completedDays.size}/7 Days Done"
                                ExperimentStatus.COMPLETED -> "Challenge Completed 🎉"
                                ExperimentStatus.NOT_STARTED -> "Ready to Start"
                            },
                            color = if (experiment.status == ExperimentStatus.NOT_STARTED) DeepTeal else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "+${experiment.ecoPointsReward} EcoPoints Reward",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(LightSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(experiment.iconName, fontSize = 28.sp)
                    }
                    Column {
                        Text(
                            text = experiment.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                        Text(
                            text = experiment.subtitle,
                            fontSize = 13.sp,
                            color = DeepTeal.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = experiment.description,
                    fontSize = 13.sp,
                    color = DeepTeal.copy(alpha = 0.85f),
                    lineHeight = 19.sp
                )

                if (experiment.status == ExperimentStatus.NOT_STARTED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onStartExperiment,
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("exp_start_button")
                    ) {
                        Text("Start This 7-Day Challenge", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        // Action Guide Instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Action Guide (Daily Habits)", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)
                experiment.actionGuide.forEachIndexed { idx, step ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = LightSage,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                            }
                        }
                        Text(step, fontSize = 12.sp, color = DeepTeal.copy(alpha = 0.85f), lineHeight = 16.sp)
                    }
                }

                Divider(color = SoftCream, modifier = Modifier.padding(vertical = 4.dp))

                Text("💡 Baseline Tip: ${experiment.baselineTip}", fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.75f))
                Text("📈 Expected Savings: ${experiment.expectedSavingTip}", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
            }
        }

        // 7-Day Checklist
        Text(
            text = "Daily Habit Ledger (7 Days)",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTeal
        )

        experiment.dailyChecks.forEach { check ->
            val isDone = check.completed
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (isDone) LightSage.copy(alpha = 0.5f) else Color.White),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isDone) FreshGreen else SoftCream),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text("${check.dayNumber}", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 13.sp)
                            }
                        }

                        Column {
                            Text(
                                text = "Day ${check.dayNumber}: ${if (isDone) "Completed" else "Pending Action"}",
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal,
                                fontSize = 14.sp
                            )
                            if (check.dateString.isNotBlank()) {
                                Text(
                                    text = "${check.dateString} ${if (check.readingKwh != null) "• ${check.readingKwh} kWh" else ""}",
                                    fontSize = 11.sp,
                                    color = DeepTeal.copy(alpha = 0.7f)
                                )
                            }
                            if (check.notes.isNotBlank()) {
                                Text(
                                    text = "“${check.notes}”",
                                    fontSize = 11.sp,
                                    color = EmeraldGreen,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (experiment.status == ExperimentStatus.ACTIVE) {
                        Button(
                            onClick = { checkDialogDay = check.dayNumber },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDone) EmeraldGreen else FreshGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("log_day_${check.dayNumber}_button")
                        ) {
                            Text(if (isDone) "Edit" else "Log", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Daily Habit Dialog
    checkDialogDay?.let { dayNum ->
        val currentCheck = experiment.dailyChecks.firstOrNull { it.dayNumber == dayNum }
        DailyHabitCheckDialog(
            dayNumber = dayNum,
            existingKwh = currentCheck?.readingKwh?.toString() ?: "",
            existingNotes = currentCheck?.notes ?: "",
            onDismiss = { checkDialogDay = null },
            onConfirm = { kwhVal, notesVal ->
                val awarded = onLogDailyCheck(dayNum, kwhVal, notesVal)
                snackbarMessage = if (awarded) "Logged Day $dayNum! Earned +10 EcoPoints 🌱" else "Updated Day $dayNum log"
                checkDialogDay = null
            }
        )
    }
}

@Composable
private fun DailyHabitCheckDialog(
    dayNumber: Int,
    existingKwh: String,
    existingNotes: String,
    onDismiss: () -> Unit,
    onConfirm: (Double?, String) -> Unit
) {
    var kwhText by remember { mutableStateOf(existingKwh) }
    var notesText by remember { mutableStateOf(existingNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Habit for Day $dayNumber", fontWeight = FontWeight.Bold, color = DeepTeal) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Confirm you practiced the habit today (e.g. thermostat kept at 24°C, standby switches cut).",
                    fontSize = 12.sp,
                    color = DeepTeal.copy(alpha = 0.8f)
                )

                OutlinedTextField(
                    value = kwhText,
                    onValueChange = { kwhText = it },
                    label = { Text("Daily Meter Reading (kWh, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("daily_check_kwh_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Observations") },
                    modifier = Modifier.fillMaxWidth().testTag("daily_check_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kwh = kwhText.toDoubleOrNull()
                    onConfirm(kwh, notesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text("Confirm & Earn EcoPoints", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DeepTeal)
            }
        }
    )
}
