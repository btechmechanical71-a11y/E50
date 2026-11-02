package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SupabaseClientProvider
import io.github.jan.supabase.auth.handleDeeplinks
import com.example.ui.components.E50BottomNav
import com.example.ui.components.E50TopBar
import com.example.ui.screens.*
import com.example.ui.screens.games.*
import com.example.ui.theme.E50Theme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.E50ViewModel

class MainActivity : ComponentActivity() {

    private val e50ViewModel: E50ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.data?.path == "/reset-password") {
            e50ViewModel.onPasswordRecoveryLink()
        }
        if (SupabaseClientProvider.isConfigured) {
            SupabaseClientProvider.client.handleDeeplinks(intent)
        }

        enableEdgeToEdge()
        setContent {
            E50Theme {
                E50App(viewModel = e50ViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.data?.path == "/reset-password") {
            e50ViewModel.onPasswordRecoveryLink()
        }
        if (SupabaseClientProvider.isConfigured) {
            SupabaseClientProvider.client.handleDeeplinks(intent)
        }
    }
}

@Composable
fun E50App(viewModel: E50ViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val householdDna by viewModel.householdDna.collectAsStateWithLifecycle()
    val footprintEntries by viewModel.footprintEntries.collectAsStateWithLifecycle()
    val experiments by viewModel.experiments.collectAsStateWithLifecycle()
    val communityBattles by viewModel.communityBattles.collectAsStateWithLifecycle()
    val impactLogs by viewModel.impactLogs.collectAsStateWithLifecycle()
    val gameScores by viewModel.gameScores.collectAsStateWithLifecycle()

    val recommendations = viewModel.getRecommendations()
    val achievements = viewModel.getAchievements()
    val cityLeaderboard = viewModel.getCityLeaderboard()
    val userLeaderboard = viewModel.getUserLeaderboard()

    // Handle Android system back gesture / back button
    BackHandler(enabled = currentScreen != AppScreen.Dashboard && currentScreen != AppScreen.Landing) {
        viewModel.navigateBack()
    }

    val isFullscreenScreen = currentScreen is AppScreen.Landing ||
            currentScreen is AppScreen.Auth ||
            currentScreen is AppScreen.Onboarding

    val showBottomNav = currentScreen is AppScreen.Dashboard ||
            currentScreen is AppScreen.EnergyDna ||
            currentScreen is AppScreen.Footprint ||
            currentScreen is AppScreen.Experiments ||
            currentScreen is AppScreen.Games

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isFullscreenScreen) {
                val screenTitle = when (currentScreen) {
                    is AppScreen.Dashboard -> "E50 Dashboard"
                    is AppScreen.EnergyDna -> "Energy DNA"
                    is AppScreen.Footprint -> "Footprint Ledger"
                    is AppScreen.Experiments -> "7-Day Energy Lab"
                    is AppScreen.ExperimentDetail -> "Challenge Detail"
                    is AppScreen.Games -> "Mission LiFE Games"
                    is AppScreen.GameEnergyDetective -> "Energy Detective"
                    is AppScreen.GameEcoSort -> "Eco Sort"
                    is AppScreen.GameCityBuilder -> "City Builder"
                    is AppScreen.CommunityBattles, is AppScreen.CityLeaderboard -> "Community Action"
                    is AppScreen.ProofOfImpactView -> "Proof of Impact"
                    is AppScreen.RewardsAchievements -> "Rewards & Badges"
                    is AppScreen.ProfileSettings -> "Citizen Profile"
                    else -> "E50"
                }

                val canBack = currentScreen !is AppScreen.Dashboard

                E50TopBar(
                    title = screenTitle,
                    canNavigateBack = canBack,
                    onNavigateBack = { viewModel.navigateBack() },
                    ecoPoints = userProfile.ecoPoints,
                    ecoCredits = userProfile.ecoCredits,
                    onProfileClick = { viewModel.navigateTo(AppScreen.ProfileSettings) }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                E50BottomNav(
                    currentScreen = currentScreen,
                    onTabSelected = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Landing -> {
                    LandingScreen(
                        onGetStarted = { viewModel.navigateTo(AppScreen.Onboarding) },
                        onSignIn = { viewModel.navigateTo(AppScreen.Auth) }
                    )
                }

                is AppScreen.Auth -> {
                    AuthScreen(
                        onAuthSuccess = { email, name, city, isGuest ->
                            viewModel.signIn(email, name, city, isGuest)
                        },
                        onBack = { viewModel.navigateTo(AppScreen.Landing) }
                    )
                }

                is AppScreen.Onboarding -> {
                    OnboardingScreen(
                        currentName = userProfile.displayName,
                        currentCity = userProfile.city,
                        onComplete = { dna, name, city ->
                            viewModel.completeOnboarding(dna, name, city)
                        },
                        onSkip = { viewModel.navigateTo(AppScreen.Dashboard) }
                    )
                }

                is AppScreen.Dashboard -> {
                    DashboardScreen(
                        userProfile = userProfile,
                        householdDna = householdDna,
                        footprintEntries = footprintEntries,
                        experiments = experiments,
                        communityBattles = communityBattles,
                        impactLogs = impactLogs,
                        recommendations = recommendations,
                        achievements = achievements,
                        onNavigateToDna = { viewModel.navigateTo(AppScreen.EnergyDna) },
                        onNavigateToFootprint = { viewModel.navigateTo(AppScreen.Footprint) },
                        onNavigateToExperiments = { viewModel.navigateTo(AppScreen.Experiments) },
                        onNavigateToExperimentDetail = { id -> viewModel.navigateTo(AppScreen.ExperimentDetail(id)) },
                        onNavigateToGames = { viewModel.navigateTo(AppScreen.Games) },
                        onNavigateToGameDetective = { viewModel.navigateTo(AppScreen.GameEnergyDetective) },
                        onNavigateToGameEcoSort = { viewModel.navigateTo(AppScreen.GameEcoSort) },
                        onNavigateToGameCityBuilder = { viewModel.navigateTo(AppScreen.GameCityBuilder) },
                        onNavigateToCommunity = { viewModel.navigateTo(AppScreen.CommunityBattles) },
                        onNavigateToProofOfImpact = { viewModel.navigateTo(AppScreen.ProofOfImpactView) },
                        onNavigateToRewards = { viewModel.navigateTo(AppScreen.RewardsAchievements) }
                    )
                }

                is AppScreen.EnergyDna -> {
                    EnergyDnaScreen(
                        dna = householdDna,
                        recommendations = recommendations,
                        onUpdateDna = { updated -> viewModel.updateHouseholdDna(updated) },
                        onStartExperiment = { expId -> viewModel.navigateTo(AppScreen.ExperimentDetail(expId)) }
                    )
                }

                is AppScreen.Footprint -> {
                    FootprintScreen(
                        entries = footprintEntries,
                        onAddEntry = { entry -> viewModel.addFootprint(entry) },
                        onUpdateEntry = { entry -> viewModel.updateFootprint(entry) },
                        onDeleteEntry = { id -> viewModel.deleteFootprint(id) }
                    )
                }

                is AppScreen.Experiments -> {
                    ExperimentsScreen(
                        experiments = experiments,
                        onSelectExperiment = { id -> viewModel.navigateTo(AppScreen.ExperimentDetail(id)) }
                    )
                }

                is AppScreen.ExperimentDetail -> {
                    val exp = experiments.firstOrNull { it.id == screen.experimentId }
                        ?: experiments.first()
                    ExperimentDetailScreen(
                        experiment = exp,
                        onBack = { viewModel.navigateBack() },
                        onStartExperiment = { viewModel.startExperiment(exp.id) },
                        onLogDailyCheck = { day, kwh, notes ->
                            viewModel.recordDailyCheck(exp.id, day, kwh, notes)
                        }
                    )
                }

                is AppScreen.Games -> {
                    GamesHubScreen(
                        gameScores = gameScores,
                        ecoCredits = userProfile.ecoCredits,
                        onLaunchEnergyDetective = { viewModel.navigateTo(AppScreen.GameEnergyDetective) },
                        onLaunchEcoSort = { viewModel.navigateTo(AppScreen.GameEcoSort) },
                        onLaunchCityBuilder = { viewModel.navigateTo(AppScreen.GameCityBuilder) }
                    )
                }

                is AppScreen.GameEnergyDetective -> {
                    GameEnergyDetectiveScreen(
                        onBack = { viewModel.navigateBack() },
                        onCompleteGame = { result ->
                            viewModel.recordGameScore(result)
                            viewModel.navigateTo(AppScreen.Games)
                        }
                    )
                }

                is AppScreen.GameEcoSort -> {
                    GameEcoSortScreen(
                        onBack = { viewModel.navigateBack() },
                        onCompleteGame = { result ->
                            viewModel.recordGameScore(result)
                            viewModel.navigateTo(AppScreen.Games)
                        }
                    )
                }

                is AppScreen.GameCityBuilder -> {
                    GameGreenCityScreen(
                        onBack = { viewModel.navigateBack() },
                        onCompleteGame = { result ->
                            viewModel.recordGameScore(result)
                            viewModel.navigateTo(AppScreen.Games)
                        }
                    )
                }

                is AppScreen.CommunityBattles, is AppScreen.CityLeaderboard -> {
                    CommunityScreen(
                        battles = communityBattles,
                        cityLeaderboard = cityLeaderboard,
                        userLeaderboard = userLeaderboard,
                        onJoinBattle = { battleId -> viewModel.joinBattle(battleId) },
                        onContributeBattle = { battleId, amount -> viewModel.contributeToBattle(battleId, amount) }
                    )
                }

                is AppScreen.ProofOfImpactView -> {
                    ProofOfImpactScreen(
                        impactLogs = impactLogs,
                        onBack = { viewModel.navigateBack() },
                        onLogAction = { action -> viewModel.logProofOfImpact(action) }
                    )
                }

                is AppScreen.RewardsAchievements -> {
                    RewardsScreen(
                        userProfile = userProfile,
                        achievements = achievements,
                        onBack = { viewModel.navigateBack() },
                        onUnlockAvatar = { avatar, cost -> viewModel.unlockAvatar(avatar, cost) },
                        onSelectAvatar = { avatar -> viewModel.selectAvatar(avatar) }
                    )
                }

                is AppScreen.ProfileSettings -> {
                    ProfileScreen(
                        userProfile = userProfile,
                        householdDna = householdDna,
                        onBack = { viewModel.navigateBack() },
                        onUpdateProfile = { name, city ->
                            viewModel.signIn(userProfile.email, name, city, userProfile.isGuest)
                        },
                        onResetData = { viewModel.resetData() },
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }
}
