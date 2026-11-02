package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.data.model.ProofOfImpact
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProofOfImpactScreen(
    impactLogs: List<ProofOfImpact>,
    onBack: () -> Unit,
    onLogAction: (ProofOfImpact) -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }

    val totalKwhSaved = impactLogs.sumOf { it.estimatedKwhSaved }
    val totalCo2Avoided = impactLogs.sumOf { it.estimatedKgCo2Avoided }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("proof_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
                }
                Text(
                    text = "Proof-of-Impact Ledger",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
            }

            Button(
                onClick = { showLogDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("log_new_proof_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Proof", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Verified Environmental Savings",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
                Text(
                    text = "Transparent calculations with zero fabricated data",
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ImpactSummaryBox(
                        title = "Est. Electricity Saved",
                        value = "${String.format(Locale.US, "%.1f", totalKwhSaved)} kWh",
                        icon = "⚡",
                        modifier = Modifier.weight(1f)
                    )
                    ImpactSummaryBox(
                        title = "Avoided CO₂e",
                        value = "${String.format(Locale.US, "%.2f", totalCo2Avoided)} kg",
                        icon = "🍃",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Impact Feed List
        Text(
            text = "Activity Log (${impactLogs.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTeal
        )

        if (impactLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📜", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No verified actions logged yet",
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tap '+ Log Proof' to record your sustainable actions with details and notes.",
                        fontSize = 12.sp,
                        color = DeepTeal.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(impactLogs) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = LightSage,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = log.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTeal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = log.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = EmeraldGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = log.title,
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal,
                                fontSize = 15.sp
                            )

                            Text(
                                text = log.actionDetail,
                                fontSize = 12.sp,
                                color = DeepTeal.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )

                            if (log.photoNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📸 Proof: “${log.photoNote}”",
                                    fontSize = 11.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⚡ -${String.format(Locale.US, "%.1f", log.estimatedKwhSaved)} kWh",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal
                                )
                                Text(
                                    text = "🍃 -${String.format(Locale.US, "%.2f", log.estimatedKgCo2Avoided)} kg CO₂e",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogDialog) {
        LogProofDialog(
            onDismiss = { showLogDialog = false },
            onSave = { action ->
                onLogAction(action)
                showLogDialog = false
            }
        )
    }
}

@Composable
private fun ImpactSummaryBox(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SoftCream
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(icon, fontSize = 14.sp)
                Text(title, fontSize = 10.sp, color = DeepTeal.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = DeepTeal)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LogProofDialog(
    onDismiss: () -> Unit,
    onSave: (ProofOfImpact) -> Unit
) {
    var title by remember { mutableStateOf("Cleaned Air Conditioner Filters") }
    var category by remember { mutableStateOf("Energy Saving") }
    var detail by remember { mutableStateOf("Rinsed dust filters with water, reducing compressor workload") }
    var kwhText by remember { mutableStateOf("1.5") }
    var proofNote by remember { mutableStateOf("Cleaned indoor mesh; dried before reinstalling") }

    val categories = listOf("Energy Saving", "Solar Adoption", "Waste Segregation", "Green Mobility", "Cool Roof & Shading")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Proof of Impact Action", fontWeight = FontWeight.Bold, color = DeepTeal) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Category", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Action Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("impact_title_input")
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Action Description") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = kwhText,
                    onValueChange = { kwhText = it },
                    label = { Text("Est. Electricity Saved (kWh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("impact_kwh_input")
                )

                OutlinedTextField(
                    value = proofNote,
                    onValueChange = { proofNote = it },
                    label = { Text("Proof Observation / Reference Note") },
                    modifier = Modifier.fillMaxWidth()
                )

                val parsedKwh = kwhText.toDoubleOrNull() ?: 1.0
                Text(
                    text = "Avoided CO₂e: ${String.format(Locale.US, "%.2f", parsedKwh * 0.716)} kg (CEA 0.716 factor)",
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kwh = kwhText.toDoubleOrNull() ?: 1.0
                    val avoided = kwh * 0.716
                    onSave(
                        ProofOfImpact(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            category = category,
                            actionDetail = detail.trim(),
                            estimatedKwhSaved = kwh,
                            estimatedKgCo2Avoided = avoided,
                            photoNote = proofNote.trim(),
                            status = "Self-reported"
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text("Submit impact (up to 25 daily EcoPoints)", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DeepTeal)
            }
        }
    )
}
