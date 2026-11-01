package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.AchievementItem
import com.example.data.model.UserProfile
import com.example.ui.theme.*

@Composable
fun RewardsScreen(
    userProfile: UserProfile,
    achievements: List<AchievementItem>,
    onBack: () -> Unit,
    onUnlockAvatar: (avatar: String, cost: Int) -> Boolean,
    onSelectAvatar: (String) -> Unit
) {
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val shopAvatars = listOf(
        Triple("🌱 Eco Pioneer", 0, "Earned on onboarding"),
        Triple("⚡ Energy Sleuth", 0, "Default detective title"),
        Triple("🍃 Green Champion", 0, "Unlocked explorer title"),
        Triple("🌳 Solar Guardian", 100, "Unlocks with 100 EcoCredits"),
        Triple("🏙️ Eco Planner", 200, "Unlocks with 200 EcoCredits"),
        Triple("👑 Net Zero Leader", 350, "Unlocks with 350 EcoCredits")
    )

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
                IconButton(onClick = onBack, modifier = Modifier.testTag("rewards_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
                }
                Text(
                    text = "Rewards & Achievements",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
            }
        }

        // Balances Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Your Sustainability Ledger",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(LightSage, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text("🌱 EcoPoints", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${userProfile.ecoPoints}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = FreshGreen)
                        Text("Verified Impact", fontSize = 10.sp, color = DeepTeal.copy(alpha = 0.7f))
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(SunGold.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text("⚡ EcoCredits", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepTeal)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${userProfile.ecoCredits}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = SunGold)
                        Text("Game Token Balance", fontSize = 10.sp, color = DeepTeal.copy(alpha = 0.7f))
                    }
                }
            }
        }

        // SnackBar Feedback if needed
        snackbarMessage?.let { msg ->
            Surface(
                color = EmeraldGreen,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Avatar Unlock Shop
            item {
                Text(
                    text = "Avatar Titles (Unlock with EcoCredits)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    shopAvatars.forEach { (avatar, cost, desc) ->
                        val isUnlocked = userProfile.unlockedAvatars.contains(avatar)
                        val isEquipped = userProfile.selectedAvatar == avatar

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isEquipped) LightSage else Color.White),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(avatar, fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 14.sp)
                                    Text(desc, fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.7f))
                                }

                                if (isEquipped) {
                                    Surface(color = FreshGreen, shape = RoundedCornerShape(8.dp)) {
                                        Text("Equipped", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                } else if (isUnlocked) {
                                    OutlinedButton(
                                        onClick = { onSelectAvatar(avatar) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("equip_${avatar.take(5)}")
                                    ) {
                                        Text("Equip", fontSize = 11.sp, color = DeepTeal, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            val ok = onUnlockAvatar(avatar, cost)
                                            snackbarMessage = if (ok) "Unlocked $avatar! 🎉" else "Need $cost EcoCredits to unlock!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SunGold),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = userProfile.ecoCredits >= cost,
                                        modifier = Modifier.testTag("unlock_${avatar.take(5)}")
                                    ) {
                                        Text("⚡ $cost", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Achievements
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Core Milestones & Badges (${achievements.count { it.isUnlocked }}/${achievements.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
            }

            items(achievements) { ach ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (ach.isUnlocked) Color.White else SoftCream.copy(alpha = 0.7f)),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (ach.isUnlocked) 1.dp else 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (ach.isUnlocked) FreshGreen.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(ach.iconName, fontSize = 22.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ach.title,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal,
                                    fontSize = 14.sp
                                )

                                Surface(
                                    color = if (ach.isUnlocked) FreshGreen else Color.LightGray.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (ach.isUnlocked) "Unlocked" else ach.progressText,
                                        fontSize = 10.sp,
                                        color = if (ach.isUnlocked) Color.White else DeepTeal,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(ach.description, fontSize = 11.sp, color = DeepTeal.copy(alpha = 0.75f), lineHeight = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
