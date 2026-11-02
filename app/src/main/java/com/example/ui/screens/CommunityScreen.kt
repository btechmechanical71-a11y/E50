package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.data.model.CityLeaderboardEntry
import com.example.data.model.CommunityBattle
import com.example.data.model.UserLeaderboardEntry
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    battles: List<CommunityBattle>,
    cityLeaderboard: List<CityLeaderboardEntry>,
    userLeaderboard: List<UserLeaderboardEntry>,
    onJoinBattle: (String) -> Unit,
    onContributeBattle: (battleId: String, amount: Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Battles, 1: City Rankings, 2: Citizen Leaders
    var contributeBattleId by remember { mutableStateOf<String?>(null) }

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
                        Text("🤝", fontSize = 22.sp)
                    }
                    Column {
                        Text(
                            text = "Community Climate Action",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = DeepTeal
                        )
                        Text(
                            text = "Mission LiFE collective citizen impact",
                            fontSize = 12.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftCream, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    listOf("Battles (${battles.size})", "City Ranking", "Citizens").forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == index) FreshGreen else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selectedTab == index) Color.White else DeepTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Transparency Disclaimer
        Surface(
            color = LightSage,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("ℹ️", fontSize = 14.sp)
                Text(
                    text = "Signed-in citizens see live E50 Cloud data. Leaderboards are opt-in. Contributions are self-reported and are not independently verified.",
                    fontSize = 11.sp,
                    color = DeepTeal.copy(alpha = 0.85f),
                    lineHeight = 15.sp
                )
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Community Battles List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(battles) { battle ->
                        BattleCard(
                            battle = battle,
                            onJoin = { onJoinBattle(battle.id) },
                            onContribute = { contributeBattleId = battle.id }
                        )
                    }
                }
            }
            1 -> {
                // City Leaderboards
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "City rankings • opted-in citizens",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    if (cityLeaderboard.isEmpty()) {
                        item {
                            Text(
                                "No citizens have opted into city rankings yet. You can enable visibility in Profile Settings.",
                                fontSize = 12.sp,
                                color = DeepTeal.copy(alpha = 0.75f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)
                            )
                        }
                    } else {
                        items(cityLeaderboard) { city ->
                            CityRankRow(city = city)
                        }
                    }
                }
            }
            2 -> {
                // User Leaderboard
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "Top citizen contributors • opted-in profiles",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(userLeaderboard) { user ->
                        UserRankRow(user = user)
                    }
                }
            }
        }
    }

    // Contribute Modal
    contributeBattleId?.let { bId ->
        val battle = battles.firstOrNull { it.id == bId }
        if (battle != null) {
            ContributeBattleDialog(
                battle = battle,
                onDismiss = { contributeBattleId = null },
                onConfirm = { amount ->
                    onContributeBattle(bId, amount)
                    contributeBattleId = null
                }
            )
        }
    }
}

@Composable
private fun BattleCard(
    battle: CommunityBattle,
    onJoin: () -> Unit,
    onContribute: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    color = LightSage,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "📍 ${battle.region}",
                        color = DeepTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${battle.daysLeft} days remaining",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlertCoral
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = battle.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )

            Text(
                text = battle.subtitle,
                fontSize = 12.sp,
                color = DeepTeal.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            val progress = (battle.currentMetric / battle.targetMetric).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = FreshGreen,
                trackColor = SoftCream
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.0f", battle.currentMetric)} / ${String.format(Locale.US, "%.0f", battle.targetMetric)} ${battle.metricUnit}",
                    fontSize = 11.sp,
                    color = DeepTeal,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${battle.participantsCount} citizens active",
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            if (battle.userContribution > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your contribution: ${String.format(Locale.US, "%.0f", battle.userContribution)} ${battle.metricUnit}",
                    fontSize = 11.sp,
                    color = EmeraldGreen,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!battle.isJoined) {
                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("join_battle_${battle.id}")
                ) {
                    Text("Join Challenge & Earn 20 EcoPoints", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onContribute,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("contribute_battle_${battle.id}")
                ) {
                    Text("+ Log Today's Personal Contribution", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun CityRankRow(city: CityLeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (city.isUserCity) LightSage else Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (city.rank <= 3) SunGold.copy(alpha = 0.3f) else SoftCream),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (city.rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#${city.rank}"
                        },
                        fontSize = if (city.rank <= 3) 14.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(city.cityName, fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 14.sp)
                        if (city.isUserCity) {
                            Surface(color = EmeraldGreen, shape = RoundedCornerShape(4.dp)) {
                                Text("Your City", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("${city.activeCitizens} opted-in citizens • ${String.format(Locale.US, "%.0f", city.totalKwhSaved)} kWh reported", fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.7f))
                }
            }

            Text(
                text = "${city.ecoPointsTotal} pts",
                fontWeight = FontWeight.Black,
                color = FreshGreen,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun UserRankRow(user: UserLeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (user.isCurrentUser) LightSage else Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "#${user.rank}",
                    fontWeight = FontWeight.Black,
                    color = DeepTeal,
                    fontSize = 13.sp,
                    modifier = Modifier.width(28.dp)
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(user.name, fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 14.sp)
                        if (user.isCurrentUser) {
                            Surface(color = FreshGreen, shape = RoundedCornerShape(4.dp)) {
                                Text("You", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    Text("${user.avatar} • ${user.city}", fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.7f))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${user.ecoPoints} pts", fontWeight = FontWeight.Black, color = FreshGreen, fontSize = 14.sp)
                Text("${user.actionsCount} actions", fontSize = 10.sp, color = DeepTeal.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun ContributeBattleDialog(
    battle: CommunityBattle,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Contribution to ${battle.region}", fontWeight = FontWeight.Bold, color = DeepTeal) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Target Metric: ${battle.targetGoal}", fontSize = 12.sp, color = DeepTeal)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Enter your contribution (${battle.metricUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("contribute_amount_input")
                )
                Text(
                    "Contribute toward your city's collective Mission LiFE target and earn EcoPoints!",
                    fontSize = 11.sp,
                    color = DeepTeal.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 1.0
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreen)
            ) {
                Text("Confirm Contribution", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = DeepTeal)
            }
        }
    )
}
