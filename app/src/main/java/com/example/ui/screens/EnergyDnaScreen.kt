package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DnaRecommendation
import com.example.data.model.HouseholdDna
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnergyDnaScreen(
    dna: HouseholdDna,
    recommendations: List<DnaRecommendation>,
    onUpdateDna: (HouseholdDna) -> Unit,
    onStartExperiment: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // DNA Header Card
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
                            Text("🧬", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "Household Energy DNA",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DeepTeal
                            )
                            Text(
                                text = "Deterministic Habit & Efficiency Model",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.testTag("dna_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit DNA", tint = DeepTeal)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profile Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("${dna.housingType} • ${dna.ownership}") }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Family of ${dna.householdSize}") }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("~${String.format(Locale.US, "%.0f", dna.monthlyKwhEstimate)} kWh/mo") }
                    )
                    if (dna.appliances.contains("Air Conditioner")) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("AC: ${dna.acUsageHours}h @ ${dna.acTempPreference}°C") }
                        )
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(if (dna.hasRooftopAccess) "Rooftop Access: Yes" else "No Rooftop Access") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Appliances: ${dna.appliances.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = DeepTeal.copy(alpha = 0.75f)
                )
            }
        }

        // Solar Readiness Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = if (dna.hasRooftopAccess) LightSage else Color.White),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LimeSage, FreshGreen)))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("☀️", fontSize = 24.sp)
                    Text(
                        text = if (dna.hasSolarInstalled) "Active Rooftop Solar Installation"
                        else if (dna.hasRooftopAccess) "PM Surya Ghar: Solar Readiness High"
                        else "Solar Access: Shared/Rented Building",
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val solarDesc = if (dna.hasSolarInstalled) {
                    "Your home already utilizes solar photovoltaic generation. Keep panels dust-free every 15 days to retain 95%+ peak generation efficiency in Gujarat sunny climate."
                } else if (dna.hasRooftopAccess) {
                    val capacity = (dna.monthlyKwhEstimate / 120.0).coerceIn(1.0, 5.0)
                    "You have dedicated roof access. Based on your ${String.format(Locale.US, "%.0f", dna.monthlyKwhEstimate)} kWh monthly baseline, a ${String.format(Locale.US, "%.1f", capacity)} kW rooftop system can generate ~${String.format(Locale.US, "%.0f", capacity * 120)} kWh/month, cutting up to 80% of your bill under Gujarat Surya Urja policy."
                } else {
                    "You do not possess individual rooftop rights. Focus on behavioral conservation and energy-efficient 5-star appliances which require zero roof access."
                }

                Text(
                    text = solarDesc,
                    fontSize = 12.sp,
                    color = DeepTeal.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }

        // Section Title: Recommendations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tailored Recommendations (${recommendations.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
            Surface(
                color = LightSage,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "BEE & GEDA Rules",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }

        // Recommendation Cards
        recommendations.forEach { rec ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (rec.priority) {
                                "High" -> AlertCoral.copy(alpha = 0.15f)
                                "Medium" -> SunGold.copy(alpha = 0.2f)
                                else -> LightSage
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${rec.priority} Priority",
                                color = when (rec.priority) {
                                    "High" -> AlertCoral
                                    "Medium" -> DeepTeal
                                    else -> EmeraldGreen
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Save ~₹${String.format(Locale.US, "%.0f", rec.potentialSavingInr)}/mo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = rec.title,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = rec.reason,
                        fontSize = 12.sp,
                        color = DeepTeal.copy(alpha = 0.75f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = SoftCream,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("👉", fontSize = 14.sp)
                            Text(
                                text = rec.suggestedAction,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DeepTeal,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (rec.relatedExperimentId != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onStartExperiment(rec.relatedExperimentId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Launch 7-Day Challenge for this Habit →", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Text(
            text = "Disclaimer: Household recommendations are generated using deterministic rule-based algorithms calibrated with Bureau of Energy Efficiency (BEE) and Central Electricity Authority (CEA) baselines. They are educational suggestions and not certified on-site engineering audits.",
            fontSize = 10.sp,
            color = DeepTeal.copy(alpha = 0.55f),
            lineHeight = 14.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
    }

    if (showEditDialog) {
        EditDnaDialog(
            currentDna = dna,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onUpdateDna(updated)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun EditDnaDialog(
    currentDna: HouseholdDna,
    onDismiss: () -> Unit,
    onSave: (HouseholdDna) -> Unit
) {
    var size by remember { mutableStateOf(currentDna.householdSize) }
    var kwh by remember { mutableStateOf(currentDna.monthlyKwhEstimate.toInt().toString()) }
    var acHours by remember { mutableStateOf(currentDna.acUsageHours) }
    var acTemp by remember { mutableStateOf(currentDna.acTempPreference) }
    var rooftop by remember { mutableStateOf(currentDna.hasRooftopAccess) }
    var solar by remember { mutableStateOf(currentDna.hasSolarInstalled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Household Energy DNA", fontWeight = FontWeight.Bold, color = DeepTeal) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Household Members: $size", fontSize = 13.sp, color = DeepTeal)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 2, 3, 4, 5, 6).forEach { s ->
                        FilterChip(selected = size == s, onClick = { size = s }, label = { Text("$s") })
                    }
                }

                OutlinedTextField(
                    value = kwh,
                    onValueChange = { kwh = it },
                    label = { Text("Monthly Electricity (kWh)") },
                    singleLine = true
                )

                Text("AC Thermostat Setting: $acTemp°C", fontSize = 13.sp, color = DeepTeal)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(18, 20, 22, 24, 26).forEach { t ->
                        FilterChip(selected = acTemp == t, onClick = { acTemp = t }, label = { Text("$t°C") })
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Rooftop Access", fontSize = 13.sp, color = DeepTeal)
                    Switch(checked = rooftop, onCheckedChange = { rooftop = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = currentDna.copy(
                        householdSize = size,
                        monthlyKwhEstimate = kwh.toDoubleOrNull() ?: currentDna.monthlyKwhEstimate,
                        acUsageHours = acHours,
                        acTempPreference = acTemp,
                        hasRooftopAccess = rooftop,
                        hasSolarInstalled = if (rooftop) solar else false
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DeepTeal)
            }
        }
    )
}
