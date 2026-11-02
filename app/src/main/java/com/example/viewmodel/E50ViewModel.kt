package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.E50Repository
import com.example.data.SupabaseClientProvider
import com.example.data.SupabaseDataRepository
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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
    private val cloudRepository = SupabaseDataRepository()
    private var cloudLoadingUserId: String? = null
    private var cloudReadyUserId: String? = null

    private val _dataSyncStatus = MutableStateFlow("Not connected")
    val dataSyncStatus: StateFlow<String> = _dataSyncStatus.asStateFlow()

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
                                ?.jsonPrimitive?.contentOrNull
                                ?.takeIf { it.isNotBlank() }
                                ?: email.substringBefore("@").ifBlank { "Eco Citizen" }
                            val city = user?.userMetadata?.get("city")
                                ?.jsonPrimitive?.contentOrNull
                                ?.takeIf { it.isNotBlank() }
                                ?: "Ahmedabad"

                            repository.loginUser(
                                email = email,
                                name = name,
                                city = city,
                                isGuest = false,
                                userId = user?.id
                            )

                            val authenticatedUserId = user?.id
                            if (
                                authenticatedUserId != null &&
                                cloudReadyUserId != authenticatedUserId &&
                                cloudLoadingUserId != authenticatedUserId
                            ) {
                                cloudLoadingUserId = authenticatedUserId
                                _dataSyncStatus.value = "Loading your cloud data..."
                                viewModelScope.launch {
                                    try {
                                        val snapshot = cloudRepository.loadUserSnapshot(
                                            userId = authenticatedUserId,
                                            email = email
                                        )
                                        val mergedExperiments = repository.experiments.value.map { local ->
                                            val remote = snapshot.experiments.firstOrNull { it.id == local.id }
                                            if (remote == null) {
                                                local
                                            } else {
                                                val remoteChecks = snapshot.dailyChecks
                                                    .filter { it.experimentId == local.id }
                                                    .associateBy { it.check.dayNumber }
                                                local.copy(
                                                    status = remote.status,
                                                    startedTimestamp = remote.startedTimestamp,
                                                    completedDays = remote.completedDays,
                                                    dailyChecks = local.dailyChecks.map { check ->
                                                        remoteChecks[check.dayNumber]?.check
                                                            ?: if (check.dayNumber in remote.completedDays) {
                                                                check.copy(completed = true)
                                                            } else {
                                                                check
                                                            }
                                                    }
                                                )
                                            }
                                        }
                                        repository.restoreCloudData(
                                            profile = snapshot.profile,
                                            dna = snapshot.householdDna,
                                            footprints = snapshot.footprints,
                                            experiments = mergedExperiments,
                                            gameScores = snapshot.gameScores,
                                            impactLogs = snapshot.impactLogs
                                        )
                                        cloudReadyUserId = authenticatedUserId
                                        _dataSyncStatus.value = "Cloud data synced"
                                    } catch (error: Exception) {
                                        _dataSyncStatus.value =
                                            "Cloud load failed: ${error.message ?: "unknown error"}"
                                    } finally {
                                        if (cloudLoadingUserId == authenticatedUserId) {
                                            cloudLoadingUserId = null
                                        }
                                    }
                                }
                            }

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
                            cloudReadyUserId = null
                            cloudLoadingUserId = null
                            if (!repository.userProfile.value.isGuest && !_authUiState.value.isPasswordRecovery) {
                                repository.signOutUser()
                                navigationBackStack.clear()
                                navigationBackStack.add(AppScreen.Landing)
                                _currentScreen.value = AppScreen.Landing
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

    private fun friendlyAuthError(error: Throwable, fallback: String): String {
        val details = generateSequence(error) { it.cause }
            .mapNotNull { it.message }
            .joinToString(" ")
            .lowercase()

        return when {
            "over_email_send_rate_limit" in details || "email rate limit exceeded" in details ->
                "Supabase's built-in email service is limited to 2 emails per hour across this project. Wait for the limit to reset, or configure custom SMTP in Supabase Authentication > SMTP Settings."

            "email_address_not_authorized" in details ->
                "Supabase's built-in email service can only send to members of your Supabase organization. Configure custom SMTP to send to other email addresses."

            "over_request_rate_limit" in details ->
                "Too many authentication requests were made. Please wait a few minutes and try again."

            else -> fallback
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
                    error = friendlyAuthError(error, "Signup failed. Please try again.")
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
                    error = friendlyAuthError(error, "Login failed. Check your email and password.")
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
                    error = friendlyAuthError(error, "Could not send the password reset email.")
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
                // Clear recovery mode before sign-out so the session observer does not treat
                // the intentional logout as a failed recovery callback.
                _authUiState.value = AuthUiState(
                    isLoading = true,
                    message = "Password updated. Returning to sign-in..."
                )
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

    private fun launchCloudSync(label: String, operation: suspend (String) -> Unit) {
        val userId = cloudReadyUserId ?: return
        viewModelScope.launch {
            try {
                operation(userId)
                _dataSyncStatus.value = "Saved $label to cloud"
            } catch (error: Exception) {
                _dataSyncStatus.value =
                    "Could not sync $label: ${error.message ?: "unknown error"}"
            }
        }
    }

    private fun syncProfile() {
        launchCloudSync("profile") { userId ->
            cloudRepository.saveProfile(repository.userProfile.value.copy(id = userId))
        }
    }

    private fun syncHouseholdDna() {
        launchCloudSync("household profile") { userId ->
            cloudRepository.saveHouseholdDna(userId, repository.householdDna.value)
        }
    }

    private fun syncExperiment(experimentId: String) {
        val experiment = repository.experiments.value.firstOrNull { it.id == experimentId } ?: return
        launchCloudSync("experiment") { userId ->
            cloudRepository.saveExperiment(userId, experiment)
        }
    }

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

    // Used for guest entry and updating the locally displayed profile fields.
    // Email/password authentication is handled only by signInWithEmail().
    fun signIn(email: String, name: String, city: String, isGuest: Boolean) {
        repository.loginUser(
            email = email.ifBlank { if (isGuest) "guest@e50.in" else repository.userProfile.value.email },
            name = name.ifBlank { if (isGuest) "Guest Citizen" else "Eco Citizen" },
            city = city.ifBlank { "Ahmedabad" },
            isGuest = isGuest
        )
        navigationBackStack.clear()
        navigationBackStack.add(AppScreen.Dashboard)
        _currentScreen.value = AppScreen.Dashboard
    }

    fun signOut() {
        cloudReadyUserId = null
        cloudLoadingUserId = null
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
        syncProfile()
        syncHouseholdDna()
        navigateTo(AppScreen.Dashboard)
    }

    fun updateHouseholdDna(dna: HouseholdDna) {
        repository.updateHouseholdDna(dna)
        syncHouseholdDna()
    }

    fun updateProfile(name: String, city: String) {
        repository.updateProfile(name, city)
        syncProfile()
    }

    fun addFootprint(entry: FootprintEntry) {
        repository.addFootprintEntry(entry)
        launchCloudSync("electricity record") { userId ->
            cloudRepository.saveFootprint(userId, entry)
        }
    }

    fun updateFootprint(entry: FootprintEntry) {
        repository.updateFootprintEntry(entry)
        launchCloudSync("electricity record") { userId ->
            cloudRepository.saveFootprint(userId, entry)
        }
    }

    fun deleteFootprint(id: String) {
        repository.deleteFootprintEntry(id)
        launchCloudSync("electricity record") { userId ->
            cloudRepository.deleteFootprint(userId, id)
        }
    }

    fun startExperiment(id: String) {
        repository.startExperiment(id)
        syncExperiment(id)
    }

    fun recordDailyCheck(experimentId: String, day: Int, kwh: Double?, notes: String): Boolean {
        val awarded = repository.recordDailyExperimentCheck(experimentId, day, kwh, notes)
        syncExperiment(experimentId)
        return awarded
    }

    fun recordGameScore(result: MiniGameResult) {
        repository.recordGameScore(result)
        launchCloudSync("game result") { userId ->
            cloudRepository.saveGameScore(userId, result)
        }
    }

    fun joinBattle(battleId: String) {
        repository.joinCommunityBattle(battleId)
    }

    fun contributeToBattle(battleId: String, amount: Double) {
        repository.recordBattleContribution(battleId, amount)
    }

    fun logProofOfImpact(impact: ProofOfImpact) {
        repository.logProofOfImpact(impact)
        launchCloudSync("impact log") { userId ->
            cloudRepository.saveImpactLog(userId, impact)
        }
    }

    fun unlockAvatar(avatar: String, cost: Int): Boolean {
        val unlocked = repository.unlockAvatar(avatar, cost)
        if (unlocked) syncProfile()
        return unlocked
    }

    fun selectAvatar(avatar: String) {
        repository.selectAvatar(avatar)
        syncProfile()
    }

    fun resetData() {
        repository.resetToDemo()
        navigateTo(AppScreen.Dashboard)
    }
}
