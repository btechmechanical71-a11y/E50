package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HouseholdDna
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    householdDna: HouseholdDna,
    dataSyncStatus: String,
    onBack: () -> Unit,
    onUpdateProfile: (name: String, city: String) -> Unit,
    onResetData: () -> Unit,
    onSignOut: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
            }
            Text(
                text = "Citizen Profile & Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
        }

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(FreshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌱", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = userProfile.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepTeal
                )

                Text(
                    text = "${userProfile.city}, Gujarat • ${userProfile.email}",
                    fontSize = 12.sp,
                    color = DeepTeal.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = LightSage,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = userProfile.selectedAvatar,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_edit_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Display Name & City", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Cloud persistence status for the signed-in account.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (dataSyncStatus.contains("failed", ignoreCase = true) ||
                        dataSyncStatus.contains("could not", ignoreCase = true)
                    ) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = if (dataSyncStatus.contains("failed", ignoreCase = true) ||
                        dataSyncStatus.contains("could not", ignoreCase = true)
                    ) AlertCoral else FreshGreen
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Cloud sync", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 13.sp)
                    Text(dataSyncStatus, color = DeepTeal.copy(alpha = 0.75f), fontSize = 12.sp)
                }
            }
        }

        // Household DNA Snapshot Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Household DNA Baseline", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                ProfileDataRow(label = "Dwelling Type", value = "${householdDna.housingType} (${householdDna.ownership})")
                ProfileDataRow(label = "Household Size", value = "${householdDna.householdSize} persons")
                ProfileDataRow(label = "Estimated Monthly kWh", value = "${String.format(Locale.US, "%.0f", householdDna.monthlyKwhEstimate)} kWh")
                ProfileDataRow(label = "AC Habits", value = "${householdDna.acUsageHours}h daily @ ${householdDna.acTempPreference}°C")
                ProfileDataRow(label = "Rooftop Solar Access", value = if (householdDna.hasRooftopAccess) "Yes (Independent Roof)" else "No / Shared")
            }
        }

        // Data Reset & Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Application Controls", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                OutlinedButton(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertCoral),
                    modifier = Modifier.fillMaxWidth().testTag("profile_reset_data_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = AlertCoral)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset App State to Initial Demo Baseline", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    modifier = Modifier.fillMaxWidth().testTag("profile_sign_out_button")
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out / Switch Mode", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // About & GEDA Mission LiFE Credits
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSage),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("E50 • 50% Efforts Towards Environment", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 13.sp)
                Text(
                    text = "Built for the Gujarat Energy Development Agency (GEDA) Mission LiFE citizen engagement challenge. " +
                            "Promoting sustainable lifestyles, household energy conservation, clean rooftop solar adoption, and civic waste management.",
                    fontSize = 11.sp,
                    color = DeepTeal.copy(alpha = 0.85f),
                    lineHeight = 15.sp
                )
            }
        }
    }

    // Edit Name & City Dialog
    if (showEditDialog) {
        var newName by remember { mutableStateOf(userProfile.displayName) }
        var newCity by remember { mutableStateOf(userProfile.city) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Update Profile", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("City (e.g. Ahmedabad, Surat, Rajkot)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(newName, newCity)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = DeepTeal)
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset to Demo State?", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Text(
                    "This will restore initial demo readings, baseline experiments, and point balances in your device storage.",
                    fontSize = 13.sp,
                    color = DeepTeal
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AlertCoral)
                ) {
                    Text("Reset Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = DeepTeal)
                }
            }
        )
    }
}

@Composable
private fun ProfileDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = DeepTeal.copy(alpha = 0.7f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
    }
}
