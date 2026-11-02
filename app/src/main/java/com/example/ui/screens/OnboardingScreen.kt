package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HouseholdDna
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    currentName: String,
    currentCity: String,
    onComplete: (dna: HouseholdDna, name: String, city: String) -> Unit,
    onSkip: () -> Unit
) {
    var name by remember { mutableStateOf(if (currentName.isBlank()) "Priya Sharma" else currentName) }
    var city by remember { mutableStateOf(if (currentCity.isBlank()) "Ahmedabad" else currentCity) }
    var householdSize by remember { mutableStateOf(4) }
    var housingType by remember { mutableStateOf("Apartment") }
    var ownership by remember { mutableStateOf("Owner") }
    var monthlyKwh by remember { mutableStateOf("260") }
    var monthlyBill by remember { mutableStateOf("1850") }

    val allAppliances = listOf(
        "Air Conditioner",
        "Refrigerator",
        "Water Heater (Geyser)",
        "Ceiling Fans",
        "Washing Machine",
        "Microwave",
        "Water Purifier (RO)",
        "Induction Cooktop"
    )
    val selectedAppliances = remember {
        mutableStateListOf("Air Conditioner", "Refrigerator", "Water Heater (Geyser)", "Ceiling Fans", "Washing Machine")
    }

    var acHours by remember { mutableStateOf(6) }
    var acTemp by remember { mutableStateOf(22) }
    var hasRooftopAccess by remember { mutableStateOf(true) }
    var hasSolarInstalled by remember { mutableStateOf(false) }

    val allInterests = listOf("Energy Conservation", "Cooling Optimization", "Solar Energy", "Waste Segregation", "Green Mobility")
    val selectedInterests = remember {
        mutableStateListOf("Energy Conservation", "Cooling Optimization", "Solar Energy")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Household Energy DNA",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = DeepTeal
                )
                Text(
                    text = "Step 1 of 1: Personalize your habits & potential",
                    fontSize = 12.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(onClick = onSkip, modifier = Modifier.testTag("onboarding_skip_button")) {
                Text("Skip", color = DeepTeal.copy(alpha = 0.7f))
            }
        }

        // Section 1: Basic Identity
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Resident Profile", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City (e.g. Ahmedabad, Surat, Rajkot)") },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_city_input"),
                    singleLine = true
                )
            }
        }

        // Section 2: Home Type & Household Size
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2. Dwelling & Occupancy", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                Text("Household Size: $householdSize persons", fontSize = 13.sp, color = DeepTeal)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 4, 5, 6).forEach { size ->
                        FilterChip(
                            selected = householdSize == size,
                            onClick = { householdSize = size },
                            label = { Text("$size") }
                        )
                    }
                }

                Text("Housing Type", fontSize = 13.sp, color = DeepTeal)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Apartment", "Independent Villa", "Row House", "Tenement").forEach { type ->
                        FilterChip(
                            selected = housingType == type,
                            onClick = { housingType = type },
                            label = { Text(type) }
                        )
                    }
                }

                Text("Occupancy Status", fontSize = 13.sp, color = DeepTeal)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Owner", "Tenant / Rental").forEach { own ->
                        FilterChip(
                            selected = ownership == own,
                            onClick = { ownership = own },
                            label = { Text(own) }
                        )
                    }
                }
            }
        }

        // Section 3: Electricity Baseline
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("3. Electricity Baseline", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                OutlinedTextField(
                    value = monthlyKwh,
                    onValueChange = { monthlyKwh = it },
                    label = { Text("Monthly Electricity (kWh / Units)") },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_kwh_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                OutlinedTextField(
                    value = monthlyBill,
                    onValueChange = { monthlyBill = it },
                    label = { Text("Estimated Monthly Electricity Bill (₹)") },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_bill_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        // Section 4: Major Appliances & AC Habits
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("4. Appliances & AC Habits", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                Text("Select Key Appliances in Home:", fontSize = 13.sp, color = DeepTeal)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    allAppliances.forEach { app ->
                        val selected = selectedAppliances.contains(app)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) selectedAppliances.remove(app) else selectedAppliances.add(app)
                            },
                            label = { Text(app, fontSize = 12.sp) }
                        )
                    }
                }

                if (selectedAppliances.contains("Air Conditioner")) {
                    Divider(color = LightSage)
                    Text("Typical AC Run Hours per Day: $acHours hrs", fontSize = 13.sp, color = DeepTeal)
                    Slider(
                        value = acHours.toFloat(),
                        onValueChange = { acHours = it.toInt() },
                        valueRange = 0f..16f,
                        steps = 15,
                        colors = SliderDefaults.colors(thumbColor = FreshGreen, activeTrackColor = FreshGreen)
                    )

                    Text("Thermostat Setting Preference: $acTemp°C", fontSize = 13.sp, color = DeepTeal)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(18, 20, 22, 24, 26).forEach { temp ->
                            FilterChip(
                                selected = acTemp == temp,
                                onClick = { acTemp = temp },
                                label = { Text("$temp°C") }
                            )
                        }
                    }
                }
            }
        }

        // Section 5: Rooftop & Solar Access
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("5. Rooftop Solar Readiness", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dedicated Rooftop Access?", fontWeight = FontWeight.SemiBold, color = DeepTeal, fontSize = 14.sp)
                        Text("For independent roof solar installation", fontSize = 12.sp, color = DeepTeal.copy(alpha = 0.7f))
                    }
                    Switch(
                        checked = hasRooftopAccess,
                        onCheckedChange = { hasRooftopAccess = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = FreshGreen, checkedTrackColor = LightSage)
                    )
                }

                if (hasRooftopAccess) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Already Installed Solar?", fontWeight = FontWeight.SemiBold, color = DeepTeal, fontSize = 14.sp)
                            Text("Solar PV grid-tied system", fontSize = 12.sp, color = DeepTeal.copy(alpha = 0.7f))
                        }
                        Switch(
                            checked = hasSolarInstalled,
                            onCheckedChange = { hasSolarInstalled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = FreshGreen, checkedTrackColor = LightSage)
                        )
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                val parsedKwh = monthlyKwh.toDoubleOrNull() ?: 260.0
                val parsedBill = monthlyBill.toDoubleOrNull() ?: 1850.0
                val dna = HouseholdDna(
                    householdSize = householdSize,
                    housingType = housingType,
                    ownership = ownership,
                    monthlyKwhEstimate = parsedKwh,
                    monthlyBillEstimate = parsedBill,
                    appliances = selectedAppliances.toList(),
                    acUsageHours = acHours,
                    acTempPreference = acTemp,
                    hasRooftopAccess = hasRooftopAccess,
                    hasSolarInstalled = hasSolarInstalled,
                    sustainabilityInterests = selectedInterests.toList()
                )
                onComplete(dna, name, city)
            },
            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_complete_button")
        ) {
            Text(
                "Save Energy DNA & Continue",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}
