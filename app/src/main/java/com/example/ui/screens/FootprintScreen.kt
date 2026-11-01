package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FootprintEntry
import com.example.ui.theme.*
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootprintScreen(
    entries: List<FootprintEntry>,
    onAddEntry: (FootprintEntry) -> Unit,
    onUpdateEntry: (FootprintEntry) -> Unit = {},
    onDeleteEntry: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<FootprintEntry?>(null) }

    val totalKwh = entries.sumOf { it.kwh }
    val totalEmissions = entries.sumOf { it.estimatedKgCo2e }
    val avgKwh = if (entries.isNotEmpty()) totalKwh / entries.size else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Energy Footprint Tracker",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                        Text(
                            text = "Verified electricity & emissions ledger",
                            fontSize = 12.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("footprint_add_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Bill", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FootprintStatBox(
                        title = "Avg Monthly",
                        value = "${String.format(Locale.US, "%.0f", avgKwh)} kWh",
                        subtitle = "Recorded Usage",
                        modifier = Modifier.weight(1f)
                    )
                    FootprintStatBox(
                        title = "Total Logged CO₂",
                        value = "${String.format(Locale.US, "%.0f", totalEmissions)} kg",
                        subtitle = "CEA v19 Baseline",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Methodology note
                Surface(
                    color = LightSage,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("ℹ️", fontSize = 14.sp)
                        Text(
                            text = "Emissions Factor: Indian National Grid weighted average of 0.716 kg CO₂e / kWh, officially sourced from CEA CO₂ Baseline Database v19 (2023-24).",
                            fontSize = 11.sp,
                            color = DeepTeal,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Usage Bar Comparison Card
        if (entries.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Monthly Consumption Visualizer",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val maxKwh = (entries.maxOfOrNull { it.kwh } ?: 300.0).coerceAtLeast(100.0)

                    entries.take(5).forEach { entry ->
                        val ratio = (entry.kwh / maxKwh).toFloat().coerceIn(0.05f, 1f)
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${entry.monthName} ${entry.year}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepTeal
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", entry.kwh)} kWh • ${String.format(Locale.US, "%.1f", entry.estimatedKgCo2e)} kg CO₂e",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = FreshGreen,
                                trackColor = SoftCream
                            )
                        }
                    }
                }
            }
        }

        // History Entries List
        Text(
            text = "Recorded Readings (${entries.size})",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTeal
        )

        if (entries.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📊", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No readings logged yet",
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Tap '+ Log Bill' above to enter your monthly electricity bill & units.",
                        fontSize = 13.sp,
                        color = DeepTeal.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            entries.forEach { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${entry.monthName} ${entry.year}",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal,
                                    fontSize = 15.sp
                                )
                                Surface(
                                    color = LightSage,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "₹${String.format(Locale.US, "%.0f", entry.billAmount)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTeal,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${String.format(Locale.US, "%.0f", entry.kwh)} kWh  →  ${String.format(Locale.US, "%.1f", entry.estimatedKgCo2e)} kg CO₂e",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreen
                            )

                            if (entry.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Notes: ${entry.notes}",
                                    fontSize = 11.sp,
                                    color = DeepTeal.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { entryToEdit = entry },
                                modifier = Modifier.testTag("footprint_edit_${entry.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Reading", tint = EmeraldGreen)
                            }
                            IconButton(
                                onClick = { onDeleteEntry(entry.id) },
                                modifier = Modifier.testTag("footprint_delete_${entry.id}")
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AlertCoral)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOrEditFootprintEntryDialog(
            initialEntry = null,
            onDismiss = { showAddDialog = false },
            onSave = { entry ->
                onAddEntry(entry)
                showAddDialog = false
            }
        )
    }

    entryToEdit?.let { entry ->
        AddOrEditFootprintEntryDialog(
            initialEntry = entry,
            onDismiss = { entryToEdit = null },
            onSave = { updated ->
                onUpdateEntry(updated)
                entryToEdit = null
            }
        )
    }
}

@Composable
private fun FootprintStatBox(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = SoftCream
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = DeepTeal)
            Text(subtitle, fontSize = 9.sp, color = DeepTeal.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun AddOrEditFootprintEntryDialog(
    initialEntry: FootprintEntry? = null,
    onDismiss: () -> Unit,
    onSave: (FootprintEntry) -> Unit
) {
    var month by remember { mutableStateOf(initialEntry?.monthName ?: "November") }
    var year by remember { mutableStateOf((initialEntry?.year ?: 2026).toString()) }
    var kwh by remember { mutableStateOf(initialEntry?.let { String.format(Locale.US, "%.0f", it.kwh) } ?: "240") }
    var bill by remember { mutableStateOf(initialEntry?.let { String.format(Locale.US, "%.0f", it.billAmount) } ?: "1720") }
    var notes by remember { mutableStateOf(initialEntry?.notes ?: "Switched to 24°C AC and unplugged bedroom standby appliances") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val isEditing = initialEntry != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEditing) "Edit Electricity Reading" else "Log Monthly Electricity Reading",
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Billing Month & Year", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("Year") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = kwh,
                    onValueChange = {
                        kwh = it
                        validationError = null
                    },
                    label = { Text("Electricity Used (kWh / Units)") },
                    modifier = Modifier.fillMaxWidth().testTag("footprint_input_kwh"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bill,
                    onValueChange = { bill = it },
                    label = { Text("Bill Amount in ₹ (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Habit notes / interventions") },
                    modifier = Modifier.fillMaxWidth()
                )

                validationError?.let { err ->
                    Text(err, color = AlertCoral, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                val parsedKwh = kwh.toDoubleOrNull() ?: 0.0
                if (parsedKwh > 0) {
                    Text(
                        text = "Estimated Emission: ${String.format(Locale.US, "%.1f", parsedKwh * 0.716)} kg CO₂e (CEA factor)",
                        fontSize = 11.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kwhVal = kwh.toDoubleOrNull() ?: 0.0
                    val billVal = bill.toDoubleOrNull() ?: 0.0
                    val yVal = year.toIntOrNull() ?: 2026
                    if (kwhVal <= 0.0) {
                        validationError = "Please enter a valid positive kWh reading"
                    } else {
                        onSave(
                            FootprintEntry(
                                id = initialEntry?.id ?: UUID.randomUUID().toString(),
                                monthName = month.trim(),
                                year = yVal,
                                kwh = kwhVal,
                                billAmount = billVal,
                                notes = notes.trim(),
                                timestamp = initialEntry?.timestamp ?: System.currentTimeMillis()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                modifier = Modifier.testTag("footprint_save_button")
            ) {
                Text(if (isEditing) "Update Reading" else "Save Reading", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DeepTeal)
            }
        }
    )
}
