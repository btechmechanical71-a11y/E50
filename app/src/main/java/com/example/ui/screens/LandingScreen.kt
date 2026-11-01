package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftCream)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FreshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "E50",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column {
                    Text(
                        text = "E50",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepTeal
                    )
                    Text(
                        text = "Mission LiFE • Gujarat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldGreen
                    )
                }
            }

            OutlinedButton(
                onClick = onSignIn,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(EmeraldGreen, FreshGreen))),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("landing_sign_in_button")
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Hero Banner Card with generated illustration
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.e50_hero_banner_1793168942297),
                    contentDescription = "Mission LiFE Green City illustration",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        color = LightSage,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "50% Efforts Towards Environment",
                            color = DeepTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Small Actions.\nMeasurable Impact.",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = DeepTeal,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Discover your household energy habits, take practical 7-day sustainability challenges, play educational green games, and make a real difference with E50.",
                        fontSize = 14.sp,
                        color = DeepTeal.copy(alpha = 0.85f),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onGetStarted,
                            colors = ButtonDefaults.buttonColors(containerColor = FreshGreen),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("landing_get_started_button")
                        ) {
                            Text(
                                text = "Get Started",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(600)
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("landing_explore_button")
                        ) {
                            Text(
                                text = "How It Works",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Value Proposition Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Interactive Features Built for You",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )

            FeatureItemRow(
                icon = "🧬",
                title = "Household Energy DNA",
                description = "Get deterministic, actionable recommendations for AC, geysers, appliances & rooftop solar."
            )

            FeatureItemRow(
                icon = "🧪",
                title = "7-Day Energy Experiments",
                description = "Structured habits: 24°C AC thermostat, vampire standby cutoff, and natural daylighting."
            )

            FeatureItemRow(
                icon = "🎮",
                title = "3 Playable Mini-Games",
                description = "Energy Detective, Eco Sort, and Green City Builder with real scores and EcoCredits."
            )

            FeatureItemRow(
                icon = "🤝",
                title = "Community Climate Battles",
                description = "Join Gujarat city challenges and track collective peak kilowatt-hour reductions."
            )

            FeatureItemRow(
                icon = "📊",
                title = "Transparent Impact Tracking",
                description = "Emissions computed with official CEA 0.716 kg CO₂e/kWh grid factor. No fake numbers."
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // EcoPoints vs EcoCredits Explainer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = LightSage),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Understanding E50 Rewards",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("🌱 EcoPoints", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Earned strictly from verified real-world actions, completed 7-day experiments, and community challenges.",
                            fontSize = 12.sp,
                            color = DeepTeal.copy(alpha = 0.8f)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("⚡ EcoCredits", fontWeight = FontWeight.Bold, color = DeepTeal, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Virtual gameplay rewards earned inside mini-games. Used to unlock cosmetic badges & avatars.",
                            fontSize = 12.sp,
                            color = DeepTeal.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // GEDA Mission LiFE & Data Transparency Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(color = DeepTeal.copy(alpha = 0.15f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Gujarat Energy Development Agency (GEDA) • Mission LiFE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTeal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Citizen Engagement Initiative for Lifestyle for Environment",
                fontSize = 11.sp,
                color = EmeraldGreen
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔒 Local Development Storage Mode: Your household records, footprint entries, and game progress are stored privately and persistently on your device. Estimates are calculated deterministically using the CEA 0.716 kg CO₂e/kWh grid factor.",
                    fontSize = 11.sp,
                    color = DeepTeal.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun FeatureItemRow(
    icon: String,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CardBorder, LightSage)))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightSage),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = DeepTeal.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
