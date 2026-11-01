package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.E50Repository
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AppScreen {
    object Landing : AppScreen()
    object Auth : AppScreen()
    object Onboarding : AppScreen()
    object Dashboard : AppScreen()
    object EnergyDna : AppScreen()
    object Footprint : AppScreen()
    object Experiments : AppScreen()
    data class ExperimentDetail(val experimentId: String) : AppScreen()
    object Games : AppScreen()
    object GameEnergyDetective : AppScreen()
    object GameEcoSort : AppScreen()
    object GameCityBuilder : AppScreen()
    object CommunityBattles : AppScreen()
    object CityLeaderboard : AppScreen()
    object ProofOfImpactView : AppScreen()
    object RewardsAchievements : AppScreen()
    object ProfileSettings : AppScreen()
}

class E50ViewModel(application: Application) : AndroidViewModel(application) {

    val repository = E50Repository(application.applicationContext)

    private val _currentScreen = MutableStateFlow<AppScreen>(
        if (repository.userProfile.value.isSignedIn) AppScreen.Dashboard else AppScreen.Landing
    )
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val navigationBackStack = mutableListOf<AppScreen>(
        if (repository.userProfile.value.isSignedIn) AppScreen.Dashboard else AppScreen.Landing
    )

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val householdDna: StateFlow<HouseholdDna> = repository.householdDna
    val footprintEntries: StateFlow<List<FootprintEntry>> = repository.footprintEntries
    val experiments: StateFlow<List<EnergyExperiment>> = repository.experiments
    val communityBattles: StateFlow<List<CommunityBattle>> = repository.communityBattles
    val impactLogs: StateFlow<List<ProofOfImpact>> = repository.impactLogs
    val gameScores: StateFlow<List<MiniGameResult>> = repository.gameScores

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            navigationBackStack.add(screen)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (navigationBackStack.size > 1) {
            navigationBackStack.removeAt(navigationBackStack.lastIndex)
            _currentScreen.value = navigationBackStack.last()
            return true
        } else if (_currentScreen.value != AppScreen.Dashboard && repository.userProfile.value.isSignedIn) {
            _currentScreen.value = AppScreen.Dashboard
            navigationBackStack.clear()
            navigationBackStack.add(AppScreen.Dashboard)
            return true
        }
        return false
    }

    fun getRecommendations(): List<DnaRecommendation> {
        return repository.generateDnaRecommendations(householdDna.value)
    }

    fun getAchievements(): List<AchievementItem> {
        return repository.getAchievements()
    }

    fun getCityLeaderboard(): List<CityLeaderboardEntry> {
        return repository.getCityLeaderboard()
    }

    fun getUserLeaderboard(): List<UserLeaderboardEntry> {
        return repository.getUserLeaderboard()
    }

    fun signIn(email: String, name: String, city: String, isGuest: Boolean) {
        repository.loginUser(
            email = if (email.isBlank()) (if (isGuest) "guest@e50.in" else "citizen@e50.in") else email,
            name = if (name.isBlank()) (if (isGuest) "Guest Citizen" else "Eco Citizen") else name,
            city = if (city.isBlank()) "Ahmedabad" else city,
            isGuest = isGuest
        )
        navigationBackStack.clear()
        navigationBackStack.add(AppScreen.Dashboard)
        _currentScreen.value = AppScreen.Dashboard
    }

    fun signOut() {
        repository.signOutUser()
        navigationBackStack.clear()
        navigationBackStack.add(AppScreen.Landing)
        _currentScreen.value = AppScreen.Landing
    }

    fun completeOnboarding(dna: HouseholdDna, name: String, city: String) {
        repository.completeOnboarding(dna, name, city)
        navigateTo(AppScreen.Dashboard)
    }

    fun updateHouseholdDna(dna: HouseholdDna) {
        repository.updateHouseholdDna(dna)
    }

    fun addFootprint(entry: FootprintEntry) {
        repository.addFootprintEntry(entry)
    }

    fun updateFootprint(entry: FootprintEntry) {
        repository.updateFootprintEntry(entry)
    }

    fun deleteFootprint(id: String) {
        repository.deleteFootprintEntry(id)
    }

    fun startExperiment(id: String) {
        repository.startExperiment(id)
    }

    fun recordDailyCheck(experimentId: String, day: Int, kwh: Double?, notes: String): Boolean {
        return repository.recordDailyExperimentCheck(experimentId, day, kwh, notes)
    }

    fun recordGameScore(result: MiniGameResult) {
        repository.recordGameScore(result)
    }

    fun joinBattle(battleId: String) {
        repository.joinCommunityBattle(battleId)
    }

    fun contributeToBattle(battleId: String, amount: Double) {
        repository.recordBattleContribution(battleId, amount)
    }

    fun logProofOfImpact(impact: ProofOfImpact) {
        repository.logProofOfImpact(impact)
    }

    fun unlockAvatar(avatar: String, cost: Int): Boolean {
        return repository.unlockAvatar(avatar, cost)
    }

    fun selectAvatar(avatar: String) {
        repository.selectAvatar(avatar)
    }

    fun resetData() {
        repository.resetToDemo()
        navigateTo(AppScreen.Dashboard)
    }
}
