package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MiniGameResult
import com.example.ui.theme.*

@Composable
fun GamesHubScreen(
    gameScores: List<MiniGameResult>,
    ecoCredits: Int,
    onLaunchEnergyDetective: () -> Unit,
    onLaunchEcoSort: () -> Unit,
    onLaunchCityBuilder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hub Banner
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
                            Text("🎮", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                text = "Mission LiFE Green Games",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = DeepTeal
                            )
                            Text(
                                text = "Play, learn, & earn virtual EcoCredits",
                                fontSize = 12.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = SunGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⚡", fontSize = 14.sp)
                            Text(
                                text = "$ecoCredits Credits",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Notice: Mini-game scores generate virtual EcoCredits used to unlock avatar badges. Game scores are educational simulations and do not measure actual household kilowatt-hours saved.",
                    fontSize = 11.sp,
                    color = DeepTeal.copy(alpha = 0.7f),
                    lineHeight = 15.sp
                )
            }
        }

        // Game 1: Energy Detective
        GameCard(
            title = "Game A: Energy Detective",
            tagline = "Spot vampire drains & inefficient cooling",
            description = "Explore virtual home rooms (Living Room, Kitchen, Bedroom, Balcony). Identify 5 subtle energy leaks and make the right green interventions.",
            icon = "🔍",
            bestScore = gameScores.filter { it.gameType == "ENERGY_DETECTIVE" }.maxOfOrNull { it.score },
            playsCount = gameScores.count { it.gameType == "ENERGY_DETECTIVE" },
            onPlay = onLaunchEnergyDetective,
            testTag = "play_game_detective_button"
        )

        // Game 2: Eco Sort
        GameCard(
            title = "Game B: Eco Sort",
            tagline = "6-Stream waste segregation master",
            description = "Sort random items into Paper, Plastic, Glass, Metal, Organic, and E-Waste bins. Build rapid accuracy streaks and avoid landfill contamination.",
            icon = "♻️",
            bestScore = gameScores.filter { it.gameType == "ECO_SORT" }.maxOfOrNull { it.score },
            playsCount = gameScores.count { it.gameType == "ECO_SORT" },
            onPlay = onLaunchEcoSort,
            testTag = "play_game_ecosort_button"
        )

        // Game 3: Green City Builder
        GameCard(
            title = "Game C: Green City Builder",
            tagline = "Construct an eco-friendly municipality",
            description = "Allocate a 1,000 Green Coin virtual municipal budget across Solar Farms, Urban Forests, Bicycle Corridors, and Recycling Centers.",
            icon = "🏙️",
            bestScore = gameScores.filter { it.gameType == "GREEN_CITY" }.maxOfOrNull { it.score },
            playsCount = gameScores.count { it.gameType == "GREEN_CITY" },
            onPlay = onLaunchCityBuilder,
            testTag = "play_game_citybuilder_button"
        )
    }
}

@Composable
private fun GameCard(
    title: String,
    tagline: String,
    description: String,
    icon: String,
    bestScore: Int?,
    playsCount: Int,
    onPlay: () -> Unit,
    testTag: String
) {
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightSage),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(icon, fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal
                        )
                        Text(
                            text = tagline,
                            fontSize = 12.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (bestScore != null) {
                    Surface(
                        color = SoftCream,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Best: $bestScore pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = DeepTeal.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (playsCount > 0) "Played $playsCount times" else "Not played yet",
                    fontSize = 11.sp,
                    color = DeepTeal.copy(alpha = 0.6f)
                )

                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag(testTag)
                ) {
                    Text("Play Now →", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
