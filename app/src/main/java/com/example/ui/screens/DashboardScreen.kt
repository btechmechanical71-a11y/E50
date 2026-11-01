package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DashboardScreen(
    userProfile: UserProfile,
    householdDna: HouseholdDna,
    footprintEntries: List<FootprintEntry>,
    experiments: List<EnergyExperiment>,
    communityBattles: List<CommunityBattle>,
    impactLogs: List<ProofOfImpact>,
    recommendations: List<DnaRecommendation>,
    achievements: List<AchievementItem>,
    onNavigateToDna: () -> Unit,
    onNavigateToFootprint: () -> Unit,
    onNavigateToExperiments: () -> Unit,
    onNavigateToExperimentDetail: (String) -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToGameDetective: () -> Unit,
    onNavigateToGameEcoSort: () -> Unit,
    onNavigateToGameCityBuilder: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToProofOfImpact: () -> Unit,
    onNavigateToRewards: () -> Unit
) {
    val scrollState = rememberScrollState()
    val activeExperiment = experiments.firstOrNull { it.status == ExperimentStatus.ACTIVE }
        ?: experiments.firstOrNull { it.status == ExperimentStatus.COMPLETED }
        ?: experiments.firstOrNull()

    val featuredBattle = communityBattles.firstOrNull { it.isJoined } ?: communityBattles.firstOrNull()
    val latestFootprint = footprintEntries.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Greeting Banner
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
                            text = "Namaste, ${userProfile.displayName}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${userProfile.city}, Gujarat • Mission LiFE",
                                fontSize = 12.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = LightSage,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onNavigateToRewards() }
                    ) {
                        Text(
                            text = userProfile.selectedAvatar,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(
                        title = "EcoPoints",
                        value = "${userProfile.ecoPoints}",
                        icon = "🌱",
                        color = FreshGreen,
                        subtitle = "Verified Action",
                        modifier = Modifier.weight(1f).clickable { onNavigateToRewards() }
                    )
                    MetricBox(
                        title = "EcoCredits",
                        value = "${userProfile.ecoCredits}",
                        icon = "⚡",
                        color = SunGold,
                        subtitle = "Game Rewards",
                        modifier = Modifier.weight(1f).clickable { onNavigateToRewards() }
                    )
                    MetricBox(
                        title = "Impact Acts",
                        value = "${impactLogs.size + experiments.sumOf { it.completedDays.size }}",
                        icon = "✅",
                        color = EmeraldGreen,
                        subtitle = "Habits Tracked",
                        modifier = Modifier.weight(1f).clickable { onNavigateToProofOfImpact() }
                    )
                }
            }
        }

        // Active 7-Day Experiment Spotlight
        if (activeExperiment != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToExperimentDetail(activeExperiment.id) }
                    .testTag("dashboard_active_experiment_card"),
                colors = CardDefaults.cardColors(containerColor = LightSage),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (activeExperiment.status == ExperimentStatus.COMPLETED) EmeraldGreen else FreshGreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (activeExperiment.status == ExperimentStatus.COMPLETED) "Completed Challenge" else "Active 7-Day Lab",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "+${activeExperiment.ecoPointsReward} EcoPoints",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = activeExperiment.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                    Text(
                        text = activeExperiment.subtitle,
                        fontSize = 13.sp,
                        color = DeepTeal.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7 Day tracker dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..7).forEach { day ->
                            val isCompleted = activeExperiment.completedDays.contains(day)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) FreshGreen else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            "$day",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepTeal
                                        )
                                    }
                                }
                                Text("D$day", fontSize = 10.sp, color = DeepTeal.copy(alpha = 0.7f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigateToExperimentDetail(activeExperiment.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (activeExperiment.status == ExperimentStatus.COMPLETED) "Review Final Report" else "Log Today's Habit (Day ${activeExperiment.completedDays.size + 1})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Energy Footprint Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToFootprint() },
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
                            text = "Electricity Footprint",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                        Text(
                            text = "Grid Emission: CEA v19 (0.716 kg CO₂e/kWh)",
                            fontSize = 11.sp,
                            color = DeepTeal.copy(alpha = 0.65f)
                        )
                    }
                    TextButton(onClick = onNavigateToFootprint) {
                        Text("View History →", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (latestFootprint != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftCream, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${latestFootprint.monthName} ${latestFootprint.year}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DeepTeal
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", latestFootprint.kwh)} kWh",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = DeepTeal
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%.0f", latestFootprint.billAmount)} bill amount",
                                fontSize = 12.sp,
                                color = DeepTeal.copy(alpha = 0.7f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = LightSage,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", latestFootprint.estimatedKgCo2e)} kg CO₂e",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Actual User Log",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Text(
                        "No electricity readings recorded yet. Record your first bill to view emissions.",
                        fontSize = 13.sp,
                        color = DeepTeal.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Mini-Games Carousel Card
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
                            text = "Play Green Mini-Games",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                        Text(
                            text = "Earn EcoCredits & master habits",
                            fontSize = 11.sp,
                            color = DeepTeal.copy(alpha = 0.65f)
                        )
                    }
                    TextButton(onClick = onNavigateToGames) {
                        Text("All Games →", color = EmeraldGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameMiniCard(
                        title = "Energy Detective",
                        emoji = "🔍",
                        subtitle = "Find vampire leaks",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGameDetective
                    )
                    GameMiniCard(
                        title = "Eco Sort",
                        emoji = "♻️",
                        subtitle = "Waste segregation",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGameEcoSort
                    )
                    GameMiniCard(
                        title = "City Builder",
                        emoji = "🏙️",
                        subtitle = "Clean grid design",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToGameCityBuilder
                    )
                }
            }
        }

        // Community Climate Battle Highlight
        if (featuredBattle != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToCommunity() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(LimeSage, FreshGreen)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                text = "🏆 ${featuredBattle.region}",
                                color = DeepTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "${featuredBattle.daysLeft} days left",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AlertCoral
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = featuredBattle.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val progress = (featuredBattle.currentMetric / featuredBattle.targetMetric).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FreshGreen,
                        trackColor = LightSage
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.0f", featuredBattle.currentMetric)} / ${String.format(Locale.US, "%.0f", featuredBattle.targetMetric)} ${featuredBattle.metricUnit}",
                            fontSize = 11.sp,
                            color = DeepTeal.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "${featuredBattle.participantsCount} citizens",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }

        // Top DNA Recommendation
        val topRec = recommendations.firstOrNull()
        if (topRec != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDna() },
                colors = CardDefaults.cardColors(containerColor = SoftCream),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CardBorder, LightSage)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🧬", fontSize = 20.sp)
                        Text(
                            text = "DNA Insight: ${topRec.category}",
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = topRec.title,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = topRec.suggestedAction,
                        fontSize = 12.sp,
                        color = DeepTeal.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Log Proof of Impact Quick Action
        Button(
            onClick = onNavigateToProofOfImpact,
            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("dashboard_log_impact_button")
        ) {
            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Environmental Action & Proof", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    icon: String,
    color: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = SoftCream
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(icon, fontSize = 14.sp)
                Text(title, fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
            Text(subtitle, fontSize = 9.sp, color = DeepTeal.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun GameMiniCard(
    title: String,
    emoji: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = SoftCream
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = DeepTeal.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}
