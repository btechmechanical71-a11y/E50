package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.model.MiniGameResult
import com.example.ui.theme.*
import java.util.UUID

data class DetectiveScenario(
    val id: String,
    val room: String,
    val title: String,
    val description: String,
    val icon: String,
    val optionA: String,
    val optionB: String,
    val correctIsB: Boolean,
    val explanation: String
)

@Composable
fun GameEnergyDetectiveScreen(
    onBack: () -> Unit,
    onCompleteGame: (MiniGameResult) -> Unit
) {
    val scenarios = remember {
        listOf(
            DetectiveScenario(
                id = "sc_standby",
                room = "Living Room",
                title = "Smart TV & Set-Top Box in Standby",
                description = "You spot a glowing red standby LED on the TV and home theater while everyone has gone to work.",
                icon = "📺",
                optionA = "Leave it on standby so it turns on instantly later",
                optionB = "Flip off the wall power socket master switch",
                correctIsB = true,
                explanation = "Flipping off the master switch eliminates continuous 10W phantom vampire drain, saving ~7-10 kWh monthly!"
            ),
            DetectiveScenario(
                id = "sc_ac_temp",
                room = "Master Bedroom",
                title = "Air Conditioner set to 18°C",
                description = "The bedroom AC is humming loudly at 18°C, while occupants are huddled under thick blankets.",
                icon = "❄️",
                optionA = "Keep at 18°C to cool the room fastest",
                optionB = "Reset thermostat to 24°C and run ceiling fan on low",
                correctIsB = true,
                explanation = "BEE research confirms setting AC to 24°C consumes ~24% less power than 18°C while providing optimal human comfort."
            ),
            DetectiveScenario(
                id = "sc_daylight",
                room = "Study Room",
                title = "Daytime Lighting Waste",
                description = "It's 11:30 AM on a bright sunny day in Ahmedabad. Curtains are drawn shut and two tube lights are burning.",
                icon = "☀️",
                optionA = "Keep lights on for steady artificial brightness",
                optionB = "Pull open curtains, turn off lights, and study near window",
                correctIsB = true,
                explanation = "Natural daylight is free, zero-carbon, and reduces eye strain. Keeping artificial lighting off saves ~0.6 kWh daily."
            ),
            DetectiveScenario(
                id = "sc_fridge",
                room = "Kitchen",
                title = "Refrigerator Door Gasket",
                description = "The refrigerator door doesn't seal tightly because a stray milk bag is wedged against the magnetic rubber seal.",
                icon = "🧊",
                optionA = "Ignore it, the compressor will automatically keep it cold",
                optionB = "Clear the obstruction and ensure the gasket seals air-tight",
                correctIsB = true,
                explanation = "Warm humid air leaking into the fridge causes frost buildup and makes the compressor run continuously, wasting ~30% more power."
            ),
            DetectiveScenario(
                id = "sc_bulb",
                room = "Balcony / Passage",
                title = "Old Filament Incandescent Bulb",
                description = "The passage fixture has an old 60-Watt tungsten incandescent bulb that feels hot to the touch.",
                icon = "💡",
                optionA = "Keep using it until the filament burns out",
                optionB = "Swap it immediately for a 9-Watt 5-star BEE LED",
                correctIsB = true,
                explanation = "A 9W LED produces the same lumens while consuming 85% less electricity and generating virtually no excess room heat."
            )
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var hasAnsweredCurrent by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isClaimed by remember { mutableStateOf(false) }

    val currentScenario = scenarios.getOrNull(currentIndex)

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("detective_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
            }
            Text(
                text = "Energy Detective",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
            Surface(
                color = LightSage,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Score: ${correctCount * 20} pts",
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (!isGameOver && currentScenario != null) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / scenarios.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = FreshGreen,
                trackColor = Color.White
            )

            // Scenario Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = LightSage,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Room: ${currentScenario.room}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Mystery ${currentIndex + 1} of ${scenarios.size}",
                            fontSize = 12.sp,
                            color = DeepTeal.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(currentScenario.icon, fontSize = 36.sp)
                        Text(
                            text = currentScenario.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentScenario.description,
                        fontSize = 14.sp,
                        color = DeepTeal.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )
                }
            }

            Text("What should the energy detective do?", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 15.sp)

            // Option A
            DetectiveOptionCard(
                text = currentScenario.optionA,
                isSelected = selectedOption == "A",
                hasAnswered = hasAnsweredCurrent,
                isCorrect = !currentScenario.correctIsB,
                onClick = {
                    if (!hasAnsweredCurrent) {
                        selectedOption = "A"
                        hasAnsweredCurrent = true
                        if (!currentScenario.correctIsB) correctCount++ else incorrectCount++
                    }
                },
                testTag = "detective_option_a"
            )

            // Option B
            DetectiveOptionCard(
                text = currentScenario.optionB,
                isSelected = selectedOption == "B",
                hasAnswered = hasAnsweredCurrent,
                isCorrect = currentScenario.correctIsB,
                onClick = {
                    if (!hasAnsweredCurrent) {
                        selectedOption = "B"
                        hasAnsweredCurrent = true
                        if (currentScenario.correctIsB) correctCount++ else incorrectCount++
                    }
                },
                testTag = "detective_option_b"
            )

            // Explanation Feedback
            if (hasAnsweredCurrent) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if ((selectedOption == "B") == currentScenario.correctIsB) LightSage else AlertCoral.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if ((selectedOption == "B") == currentScenario.correctIsB) "✅ Excellent Deduction!" else "❌ Energy Drain Detected!",
                            fontWeight = FontWeight.Black,
                            color = DeepTeal,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentScenario.explanation,
                            fontSize = 13.sp,
                            color = DeepTeal,
                            lineHeight = 18.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentIndex + 1 < scenarios.size) {
                            currentIndex++
                            selectedOption = null
                            hasAnsweredCurrent = false
                        } else {
                            isGameOver = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("detective_next_button")
                ) {
                    Text(
                        if (currentIndex + 1 < scenarios.size) "Next Room Investigation →" else "Complete Investigation & View Score",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

        } else {
            // Game Over Summary Screen
            val finalScore = correctCount * 20
            val creditsEarned = 10

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Investigation Completed!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepTeal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You identified $correctCount out of ${scenarios.size} household energy drains correctly.",
                        fontSize = 13.sp,
                        color = DeepTeal.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = LightSage,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Game Score", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                            Text("$finalScore / 100", fontSize = 32.sp, fontWeight = FontWeight.Black, color = FreshGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Up to $creditsEarned EcoCredits ⚡ (daily game limit)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (!isClaimed) {
                                isClaimed = true
                                val result = MiniGameResult(
                                    id = UUID.randomUUID().toString(),
                                    gameType = "ENERGY_DETECTIVE",
                                    score = finalScore,
                                    accuracyPercent = (correctCount * 100) / scenarios.size,
                                    ecoCreditsEarned = creditsEarned,
                                    summary = "Found $correctCount/${scenarios.size} energy leaks in Energy Detective"
                                )
                                onCompleteGame(result)
                            }
                        },
                        enabled = !isClaimed,
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("detective_claim_button")
                    ) {
                        Text(if (isClaimed) "EcoCredits Claimed ✓" else "Claim Daily EcoCredits Reward & Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            currentIndex = 0
                            selectedOption = null
                            hasAnsweredCurrent = false
                            correctCount = 0
                            incorrectCount = 0
                            isGameOver = false
                            isClaimed = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Replay Investigation", color = DeepTeal, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetectiveOptionCard(
    text: String,
    isSelected: Boolean,
    hasAnswered: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor = when {
        hasAnswered && isCorrect -> LightSage
        hasAnswered && isSelected && !isCorrect -> AlertCoral.copy(alpha = 0.2f)
        isSelected -> SoftCream
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !hasAnswered) { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected || (hasAnswered && isCorrect)) FreshGreen else SoftCream),
                contentAlignment = Alignment.Center
            ) {
                if (hasAnswered && isCorrect) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else if (hasAnswered && isSelected && !isCorrect) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                text = text,
                fontSize = 13.sp,
                color = DeepTeal,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
