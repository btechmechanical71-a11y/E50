package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyCheck
import com.example.data.model.EnergyExperiment
import com.example.data.model.ExperimentStatus
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentsScreen(
    experiments: List<EnergyExperiment>,
    onSelectExperiment: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filtered = when (selectedFilter) {
        "ACTIVE" -> experiments.filter { it.status == ExperimentStatus.ACTIVE }
        "COMPLETED" -> experiments.filter { it.status == ExperimentStatus.COMPLETED }
        "AVAILABLE" -> experiments.filter { it.status == ExperimentStatus.NOT_STARTED }
        else -> experiments
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LightSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧪", fontSize = 22.sp)
                    }
                    Column {
                        Text(
                            text = "7-Day Energy Lab",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                        Text(
                            text = "Micro-experiments for lasting household efficiency",
                            fontSize = 12.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Filter Chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ALL" to "All (${experiments.size})",
                           "ACTIVE" to "Active (${experiments.count { it.status == ExperimentStatus.ACTIVE }})",
                           "COMPLETED" to "Done (${experiments.count { it.status == ExperimentStatus.COMPLETED }})",
                           "AVAILABLE" to "New (${experiments.count { it.status == ExperimentStatus.NOT_STARTED }})").forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        }

        // List of Experiments
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered) { exp ->
                ExperimentCard(
                    experiment = exp,
                    onClick = { onSelectExperiment(exp.id) }
                )
            }
        }
    }
}

@Composable
private fun ExperimentCard(
    experiment: EnergyExperiment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("experiment_card_${experiment.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            ExperimentStatus.ACTIVE -> "In Progress (${experiment.completedDays.size}/7 Days)"
                            ExperimentStatus.COMPLETED -> "Challenge Completed"
                            ExperimentStatus.NOT_STARTED -> "Available Challenge"
                        },
                        color = if (experiment.status == ExperimentStatus.NOT_STARTED) DeepTeal else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "+${experiment.ecoPointsReward} EcoPoints",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(experiment.iconName, fontSize = 26.sp)
                Column {
                    Text(
                        text = experiment.title,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        fontSize = 16.sp
                    )
                    Text(
                        text = experiment.subtitle,
                        fontSize = 12.sp,
                        color = DeepTeal.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Daily progress dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (1..7).forEach { day ->
                    val isDone = experiment.completedDays.contains(day)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isDone) FreshGreen else SoftCream)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Estimated Impact: ${experiment.expectedSavingTip}",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldGreen
            )
        }
    }
}
