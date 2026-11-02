package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.E50Repository
import com.example.data.SupabaseClientProvider
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class AuthUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    val isPasswordRecovery: Boolean = false
)

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

    // Always begin at Landing until Supabase confirms that a saved session is valid.
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Landing)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val navigationBackStack = mutableListOf<AppScreen>(AppScreen.Landing)

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    init {
        observeSupabaseSession()
    }

    private fun observeSupabaseSession() {
        if (!SupabaseClientProvider.isConfigured) {
            _authUiState.value = AuthUiState(
                error = "Supabase is not configured. Check the private SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY settings."
            )
            return
        }

        viewModelScope.launch {
            try {
                SupabaseClientProvider.client.auth.sessionStatus.collect { status ->
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            val user = status.session.user
                            val email = user?.email.orEmpty()
                            val name = user?.userMetadata?.get("display_name")
                                ?.toString()?.removeSurrounding("\\\"")
                                ?.takeIf { it.isNotBlank() }
                                ?: email.substringBefore("@").ifBlank { "Eco Citizen" }
                            val city = user?.userMetadata?.get("city")
                                ?.toString()?.removeSurrounding("\\\"")
                                ?.takeIf { it.isNotBlank() }
                                ?: "Ahmedabad"

                            repository.loginUser(
                                email = email,
                                name = name,
                                city = city,
                                isGuest = false,
                                userId = user?.id
                            )

                            if (_authUiState.value.isPasswordRecovery) {
                                _authUiState.value = _authUiState.value.copy(
                                    isLoading = false,
                                    message = "Your reset link is verified. Enter a new password."
                                )
                                navigateTo(AppScreen.Auth)
                            } else {
                                _authUiState.value = AuthUiState()
                                navigationBackStack.clear()
                                navigationBackStack.add(AppScreen.Dashboard)
                                _currentScreen.value = AppScreen.Dashboard
                            }
                        }

                        is SessionStatus.NotAuthenticated -> {
                            if (!repository.userProfile.value.isGuest) {
                                repository.signOutUser()
                                if (!_authUiState.value.isPasswordRecovery) {
                                    navigationBackStack.clear()
                                    navigationBackStack.add(AppScreen.Landing)
                                    _currentScreen.value = AppScreen.Landing
                                } else {
                                    _authUiState.value = _authUiState.value.copy(
                                        isLoading = false,
                                        error = "The password reset link could not be verified. Request a new link."
                                    )
                                }
                            }
                        }

                        else -> Unit
                    }
                }
            } catch (error: Exception) {
                _authUiState.value = AuthUiState(
                    error = error.message ?: "Unable to initialize Supabase authentication."
                )
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, name: String, city: String) {
        if (!SupabaseClientProvider.isConfigured) {
            _authUiState.value = AuthUiState(error = "Supabase credentials are not configured.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            try {
                SupabaseClientProvider.client.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = password
                    data = buildJsonObject {
                        put("display_name", name.trim())
                        put("city", city.trim())
                    }
                }

                val signedIn = SupabaseClientProvider.client.auth.currentSessionOrNull() != null
                _authUiState.value = if (signedIn) {
                    AuthUiState(message = "Your account has been created successfully.")
                } else {
                    AuthUiState(message = "Account created. Please check your email and confirm your address before signing in.")
                }
            } catch (error: Exception) {
                _authUiState.value = AuthUiState(
                    error = error.message ?: "Signup failed. Please try again."
                )
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (!SupabaseClientProvider.isConfigured) {
            _authUiState.value = AuthUiState(error = "Supabase credentials are not configured.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            try {
                SupabaseClientProvider.client.auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password
                }
                _authUiState.value = AuthUiState()
            } catch (error: Exception) {
                _authUiState.value = AuthUiState(
                    error = error.message ?: "Login failed. Check your email and password."
                )
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (!SupabaseClientProvider.isConfigured) {
            _authUiState.value = AuthUiState(error = "Supabase credentials are not configured.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState(isLoading = true)
            try {
                SupabaseClientProvider.client.auth.resetPasswordForEmail(
                    email = email.trim(),
                    redirectUrl = "com.aistudio.e50.ecoact://auth-callback/reset-password"
                )
                _authUiState.value = AuthUiState(
                    message = "Password reset instructions have been sent. Check your email."
                )
            } catch (error: Exception) {
                _authUiState.value = AuthUiState(
                    error = error.message ?: "Could not send the password reset email."
                )
            }
        }
    }

    fun onPasswordRecoveryLink() {
        _authUiState.value = AuthUiState(
            isLoading = true,
            message = "Verifying your password reset link...",
            isPasswordRecovery = true
        )
        navigationBackStack.clear()
        navigationBackStack.add(AppScreen.Auth)
        _currentScreen.value = AppScreen.Auth
    }

    fun updatePassword(newPassword: String) {
        if (!SupabaseClientProvider.isConfigured) {
            _authUiState.value = AuthUiState(error = "Supabase credentials are not configured.")
            return
        }

        viewModelScope.launch {
            _authUiState.value = _authUiState.value.copy(isLoading = true, error = null)
            try {
                SupabaseClientProvider.client.auth.updateUser {
                    password = newPassword
                }
                SupabaseClientProvider.client.auth.signOut()
                _authUiState.value = AuthUiState(
                    message = "Password updated successfully. Sign in with your new password."
                )
                navigationBackStack.clear()
                navigationBackStack.add(AppScreen.Landing)
                _currentScreen.value = AppScreen.Landing
            } catch (error: Exception) {
                _authUiState.value = _authUiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Could not update your password."
                )
            }
        }
    }

    fun clearAuthMessage() {
        _authUiState.value = _authUiState.value.copy(message = null, error = null)
    }

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

    // Local guest entry remains available; real accounts must use Supabase Auth.
    fun signIn(email: String, name: String, city: String, isGuest: Boolean) {
        if (!isGuest) return

        repository.loginUser(
            email = email.ifBlank { "guest@e50.in" },
            name = name.ifBlank { "Guest Citizen" },
            city = city.ifBlank { "Ahmedabad" },
            isGuest = true
        )
        navigationBackStack.clear()
        navigationBackStack.add(AppScreen.Dashboard)
        _currentScreen.value = AppScreen.Dashboard
    }

    fun signOut() {
        viewModelScope.launch {
            if (SupabaseClientProvider.isConfigured && !repository.userProfile.value.isGuest) {
                runCatching { SupabaseClientProvider.client.auth.signOut() }
            }
            repository.signOutUser()
            navigationBackStack.clear()
            navigationBackStack.add(AppScreen.Landing)
            _currentScreen.value = AppScreen.Landing
            _authUiState.value = AuthUiState()
        }
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
