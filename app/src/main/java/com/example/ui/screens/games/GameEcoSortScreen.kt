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

data class WasteItem(
    val id: String,
    val name: String,
    val emoji: String,
    val correctCategory: WasteCategory,
    val hint: String
)

enum class WasteCategory(val displayName: String, val binEmoji: String, val color: Color) {
    ORGANIC("Organic Waste", "🍏", FreshGreen),
    PAPER("Paper & Pulp", "📦", SunGold),
    PLASTIC("Plastic", "🧴", AlertCoral),
    GLASS("Glass", "🍶", EcoCyan),
    METAL("Metal / Cans", "🥫", Color(0xFF7E8B92)),
    E_WASTE("E-Waste", "🔋", Color(0xFF9C27B0))
}

@Composable
fun GameEcoSortScreen(
    onBack: () -> Unit,
    onCompleteGame: (MiniGameResult) -> Unit
) {
    val items = remember {
        listOf(
            WasteItem("item_1", "Banana Peels & Fruit Scraps", "🍌", WasteCategory.ORGANIC, "Decomposes into rich compost."),
            WasteItem("item_2", "Delivery Cardboard Carton", "📦", WasteCategory.PAPER, "100% recyclable into new paper products."),
            WasteItem("item_3", "Empty PET Water Bottle", "🧴", WasteCategory.PLASTIC, "Rinse and crush for mechanical recycling."),
            WasteItem("item_4", "Cracked Glass Jar", "🫙", WasteCategory.GLASS, "Infinite recyclable silica material."),
            WasteItem("item_5", "Soda Aluminum Can", "🥫", WasteCategory.METAL, "Recycling aluminum saves 95% energy vs raw ore."),
            WasteItem("item_6", "Dead Lithium Phone Battery", "🔋", WasteCategory.E_WASTE, "Hazardous e-waste; needs authorized recycler."),
            WasteItem("item_7", "Morning Gujarati Newspaper", "📰", WasteCategory.PAPER, "High-grade recyclable cellulose."),
            WasteItem("item_8", "Vegetable Trimmings & Coriander", "🥬", WasteCategory.ORGANIC, "Wet biodegradable kitchen waste."),
            WasteItem("item_9", "Plastic Milk Pouch", "🥛", WasteCategory.PLASTIC, "Clean HDPE/LDPE plastic polymer."),
            WasteItem("item_10", "Burnt Out LED Bulb PCB", "💡", WasteCategory.E_WASTE, "Contains electronic circuits and heavy metals.")
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var streak by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var incorrectCount by remember { mutableStateOf(0) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isCorrectFeedback by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }
    var isClaimed by remember { mutableStateOf(false) }

    val currentItem = items.getOrNull(currentIndex)

    fun handleBinSelected(category: WasteCategory) {
        if (currentItem == null) return

        if (currentItem.correctCategory == category) {
            streak++
            val ptsEarned = 10 + (streak * 2)
            score += ptsEarned
            correctCount++
            isCorrectFeedback = true
            feedbackMessage = "Correct! ${currentItem.name} → ${category.displayName}. (+${ptsEarned} pts)"
        } else {
            streak = 0
            incorrectCount++
            isCorrectFeedback = false
            feedbackMessage = "Incorrect! ${currentItem.name} belongs in ${currentItem.correctCategory.displayName}. (${currentItem.hint})"
        }

        if (currentIndex + 1 < items.size) {
            currentIndex++
        } else {
            isGameOver = true
        }
    }

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
            IconButton(onClick = onBack, modifier = Modifier.testTag("ecosort_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
            }
            Text(
                text = "Eco Sort: 6-Stream Waste",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
            Surface(
                color = LightSage,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Score: $score",
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        if (!isGameOver && currentItem != null) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item ${currentIndex + 1} of ${items.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
                if (streak > 1) {
                    Surface(
                        color = SunGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🔥 ${streak}x Streak Bonus!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Target Waste Item Card
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
                    Text(currentItem.emoji, fontSize = 54.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentItem.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepTeal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap the correct waste segregation bin below:",
                        fontSize = 12.sp,
                        color = DeepTeal.copy(alpha = 0.7f)
                    )
                }
            }

            // Feedback Toast if present
            feedbackMessage?.let { msg ->
                Surface(
                    color = if (isCorrectFeedback) LightSage else AlertCoral.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // 6 Bins Grid (2 columns x 3 rows)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WasteCategory.values().toList().chunked(2).forEach { rowBins ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowBins.forEach { bin ->
                            BinCard(
                                category = bin,
                                onClick = { handleBinSelected(bin) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

        } else {
            // Completion Screen
            val creditsEarned = (score * 1.5).toInt()
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
                    Text("♻️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sorting Session Complete!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepTeal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "You correctly segregated $correctCount out of ${items.size} items.",
                        fontSize = 13.sp,
                        color = DeepTeal.copy(alpha = 0.8f)
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
                            Text("Total Score", fontSize = 12.sp, color = DeepTeal, fontWeight = FontWeight.SemiBold)
                            Text("$score pts", fontSize = 32.sp, fontWeight = FontWeight.Black, color = FreshGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+ $creditsEarned EcoCredits Earned ⚡", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (!isClaimed) {
                                isClaimed = true
                                val result = MiniGameResult(
                                    id = UUID.randomUUID().toString(),
                                    gameType = "ECO_SORT",
                                    score = score,
                                    accuracyPercent = (correctCount * 100) / items.size,
                                    ecoCreditsEarned = creditsEarned,
                                    summary = "Sorted $correctCount/${items.size} waste items with $score pts"
                                )
                                onCompleteGame(result)
                            }
                        },
                        enabled = !isClaimed,
                        colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("ecosort_claim_button")
                    ) {
                        Text(if (isClaimed) "EcoCredits Claimed ✓" else "Claim $creditsEarned EcoCredits & Save", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            currentIndex = 0
                            score = 0
                            streak = 0
                            correctCount = 0
                            incorrectCount = 0
                            feedbackMessage = null
                            isGameOver = false
                            isClaimed = false
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Play Again", color = DeepTeal, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BinCard(
    category: WasteCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("bin_${category.name.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = SoftCream),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(category.binEmoji, fontSize = 24.sp)
            Column {
                Text(
                    text = category.displayName,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    fontSize = 12.sp
                )
                Text(
                    text = "Tap to Sort",
                    fontSize = 10.sp,
                    color = category.color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
