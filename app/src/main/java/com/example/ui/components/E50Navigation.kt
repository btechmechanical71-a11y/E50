package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun E50TopBar(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    ecoPoints: Int,
    ecoCredits: Int,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier
            .background(SoftCream)
            .statusBarsPadding(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SoftCream,
            titleContentColor = DeepTeal,
            navigationIconContentColor = DeepTeal,
            actionIconContentColor = DeepTeal
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(FreshGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "E50",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                    Text(
                        text = "Mission LiFE • GEDA",
                        fontSize = 11.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = DeepTeal
                    )
                }
            }
        },
        actions = {
            // EcoPoints pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LightSage,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .clickable { onProfileClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🌱", fontSize = 12.sp)
                    Text(
                        text = "$ecoPoints",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                }
            }

            // EcoCredits pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = LimeSage.copy(alpha = 0.5f),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable { onProfileClick() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⚡", fontSize = 12.sp)
                    Text(
                        text = "$ecoCredits",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                }
            }

            IconButton(
                onClick = onProfileClick,
                modifier = Modifier.testTag("top_bar_profile_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = DeepTeal,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    )
}

@Composable
fun E50BottomNav(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = currentScreen is AppScreen.Dashboard,
            onClick = { onTabSelected(AppScreen.Dashboard) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = DeepTeal,
                indicatorColor = EmeraldGreen,
                unselectedIconColor = DeepTeal.copy(alpha = 0.6f),
                unselectedTextColor = DeepTeal.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_tab_dashboard")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Bolt, contentDescription = "Energy DNA") },
            label = { Text("DNA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = currentScreen is AppScreen.EnergyDna,
            onClick = { onTabSelected(AppScreen.EnergyDna) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = DeepTeal,
                indicatorColor = EmeraldGreen,
                unselectedIconColor = DeepTeal.copy(alpha = 0.6f),
                unselectedTextColor = DeepTeal.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_tab_dna")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Insights, contentDescription = "Footprint") },
            label = { Text("Footprint", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = currentScreen is AppScreen.Footprint,
            onClick = { onTabSelected(AppScreen.Footprint) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = DeepTeal,
                indicatorColor = EmeraldGreen,
                unselectedIconColor = DeepTeal.copy(alpha = 0.6f),
                unselectedTextColor = DeepTeal.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_tab_footprint")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Science, contentDescription = "Experiments") },
            label = { Text("7-Day Lab", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = currentScreen is AppScreen.Experiments || currentScreen is AppScreen.ExperimentDetail,
            onClick = { onTabSelected(AppScreen.Experiments) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = DeepTeal,
                indicatorColor = EmeraldGreen,
                unselectedIconColor = DeepTeal.copy(alpha = 0.6f),
                unselectedTextColor = DeepTeal.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_tab_experiments")
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Green Games") },
            label = { Text("Games", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            selected = currentScreen is AppScreen.Games ||
                    currentScreen is AppScreen.GameEnergyDetective ||
                    currentScreen is AppScreen.GameEcoSort ||
                    currentScreen is AppScreen.GameCityBuilder,
            onClick = { onTabSelected(AppScreen.Games) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = DeepTeal,
                indicatorColor = EmeraldGreen,
                unselectedIconColor = DeepTeal.copy(alpha = 0.6f),
                unselectedTextColor = DeepTeal.copy(alpha = 0.6f)
            ),
            modifier = Modifier.testTag("nav_tab_games")
        )
    }
}
