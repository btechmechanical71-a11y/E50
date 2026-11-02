package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

data class CityInfrItem(
    val id: String,
    val name: String,
    val emoji: String,
    val costCoins: Int,
    val scoreImpact: Int,
    val category: String,
    val benefitDesc: String
)

@Composable
fun GameGreenCityScreen(
    onBack: () -> Unit,
    onCompleteGame: (MiniGameResult) -> Unit
) {
    val catalog = remember {
        listOf(
            CityInfrItem("infra_solar", "Rooftop & Micro-Grid Solar", "☀️", 250, 25, "Energy", "Powers 400 municipal homes with clean zero-carbon solar."),
            CityInfrItem("infra_forest", "Urban Miyawaki Forest", "🌳", 150, 20, "Green Cover", "Cools heat islands & restores local biodiversity."),
            CityInfrItem("infra_bike", "Dedicated Bike & EV Corridors", "🚲", 150, 18, "Mobility", "Cuts peak vehicular tailpipe emissions and congestion."),
            CityInfrItem("infra_recycling", "Circular Waste Material Recovery", "♻️", 200, 22, "Waste", "Diverts 80% dry waste away from city landfills."),
            CityInfrItem("infra_park", "Public Green Waterfront Park", "🏞️", 250, 25, "Civic Space", "Permeable sponge park for rainwater recharge & wellness.")
        )
    }

    var availableCoins by remember { mutableStateOf(1000) }
    val builtCounts = remember { mutableStateMapOf<String, Int>() }
    var isEvaluated by remember { mutableStateOf(false) }
    var isClaimed by remember { mutableStateOf(false) }

    fun buildItem(item: CityInfrItem) {
        if (availableCoins >= item.costCoins) {
            availableCoins -= item.costCoins
            builtCounts[item.id] = (builtCounts[item.id] ?: 0) + 1
        }
    }

    fun removeItem(item: CityInfrItem) {
        val count = builtCounts[item.id] ?: 0
        if (count > 0) {
            builtCounts[item.id] = count - 1
            availableCoins += item.costCoins
        }
    }

    val totalScore = catalog.sumOf { (builtCounts[it.id] ?: 0) * it.scoreImpact }
    val normalizedScore = totalScore.coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("citybuilder_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
            }
            Text(
                text = "Green City Builder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
            Surface(
                color = LightSage,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🪙 $availableCoins Coins Left",
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Live Simulation Dashboard Card
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
                            text = "Eco-City Sustainability Index",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                        Text(
                            text = "Simulation: allocate budget across green infrastructure",
                            fontSize = 11.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "$normalizedScore / 100",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = FreshGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (normalizedScore / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = FreshGreen,
                    trackColor = SoftCream
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Visual Representation of Placed Assets
                val allPlaced = catalog.flatMap { item ->
                    List(builtCounts[item.id] ?: 0) { item.emoji }
                }

                if (allPlaced.isNotEmpty()) {
                    Text(
                        text = "Built Infrastructure (${allPlaced.size} modules):",
                        fontSize = 12.sp,
                        color = DeepTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftCream, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allPlaced.forEach { emoji ->
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Your city is empty. Spend Green Coins below to construct sustainable facilities!",
                        fontSize = 12.sp,
                        color = DeepTeal.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (!isEvaluated) {
            Text(
                text = "Municipal Infrastructure Catalogue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )

            // Infrastructure Items
            catalog.forEach { item ->
                val count = builtCounts[item.id] ?: 0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(item.emoji, fontSize = 28.sp)
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 14.sp)
                                    Text("🪙 ${item.costCoins} Coins • +${item.scoreImpact} Score", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // +/- Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { removeItem(item) },
                                    enabled = count > 0,
                                    modifier = Modifier.size(32.dp).testTag("city_remove_${item.id}")
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = if (count > 0) AlertCoral else Color.Gray)
                                }

                                Text("$count", fontWeight = FontWeight.Black, fontSize = 15.sp, color = DeepTeal)

                                IconButton(
                                    onClick = { buildItem(item) },
                                    enabled = availableCoins >= item.costCoins,
                                    modifier = Modifier.size(32.dp).testTag("city_add_${item.id}")
                                ) {
                                    Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = if (availableCoins >= item.costCoins) FreshGreen else Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.benefitDesc, fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.75f), lineHeight = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { isEvaluated = true },
                enabled = builtCounts.values.sum() > 0,
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("citybuilder_evaluate_button")
            ) {
                Text("Finalize City Design & Evaluate Rating", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

        } else {
            // Evaluation & Rating Card
            val ratingBadge = when {
                normalizedScore >= 80 -> "Platinum Eco-City 🏆"
                normalizedScore >= 60 -> "Gold Sustainable Town 🥇"
                else -> "Silver Developing District 🥈"
            }
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
                    Text("🏙️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Municipal Plan Evaluated!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepTeal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = LightSage,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = ratingBadge,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Your balanced allocation between clean energy, urban cooling, and circular waste earned a sustainability score of $normalizedScore/100.",
                        fontSize = 13.sp,
                        color = DeepTeal.copy(alpha = 0.8f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = SoftCream,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Virtual Gameplay Reward", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                            Text("Up to $creditsEarned EcoCredits ⚡ (daily game limit)", fontSize = 24.sp, fontWeight = FontWeight.Black, color = FreshGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (!isClaimed) {
                                isClaimed = true
                                val result = MiniGameResult(
                                    id = UUID.randomUUID().toString(),
                                    gameType = "GREEN_CITY",
                                    score = normalizedScore,
                                    accuracyPercent = normalizedScore,
                                    ecoCreditsEarned = creditsEarned,
                                    summary = "Built $ratingBadge with score $normalizedScore/100"
                                )
                                onCompleteGame(result)
                            }
                        },
                        enabled = !isClaimed,
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("citybuilder_claim_button")
                    ) {
                        Text(if (isClaimed) "EcoCredits Claimed ✓" else "Claim Daily EcoCredits Reward & Save Plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            availableCoins = 1000
                            builtCounts.clear()
                            isEvaluated = false
                            isClaimed = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Reset & Build New Layout", color = DeepTeal, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
