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

                            if (_authUiState.value.isPasswordRecovery) {
                                _authUiState.value = _authUiState.value.copy(
                                    isLoading = false,
                                    message = "Your reset link is verified. Enter a new password."
                                )
                                navigateTo(AppScreen.Auth)
                            }

                            val authenticatedUserId = user?.id
                            if (authenticatedUserId != null && cloudReadyUserId == authenticatedUserId) {
                                if (!_authUiState.value.isPasswordRecovery) {
                                    navigateAfterCloudLoad()
                                }
                            } else if (
                                authenticatedUserId != null &&
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
                                        // Ignore a late response if the user switched accounts while it loaded.
                                        val activeProfile = repository.userProfile.value
                                        if (
                                            !activeProfile.isSignedIn ||
                                            activeProfile.isGuest ||
                                            activeProfile.id != authenticatedUserId
                                        ) {
                                            return@launch
                                        }
                                        // Keep the signed-in user's device history when the cloud tables
                                        // are still empty, then migrate that history to Supabase.
                                        val localProfile = repository.userProfile.value
                                        val demoFootprintIds = setOf(
                                            "fp_oct_2026", "fp_sep_2026", "fp_aug_2026"
                                        )
                                        val demoImpactIds = setOf("imp_1", "imp_2")

                                        val localFootprints = repository.footprintEntries.value
                                            .filterNot { it.id in demoFootprintIds }
                                        val localImpactLogs = repository.impactLogs.value
                                            .filterNot { it.id in demoImpactIds }
                                        val localGameScores = repository.gameScores.value

                                        val mergedFootprints = (snapshot.footprints + localFootprints)
                                            .distinctBy { "${it.monthName.trim().lowercase()}-${it.year}" }
                                            .sortedByDescending { it.timestamp }

                                        val mergedExperiments = repository.experiments.value.map { local ->
                                            val remote = snapshot.experiments.firstOrNull { it.id == local.id }
                                            val remoteChecks = snapshot.dailyChecks
                                                .filter { it.experimentId == local.id }
                                                .associateBy { it.check.dayNumber }
                                            val completedDays = (local.completedDays + remote?.completedDays.orEmpty())
                                                .distinct()
                                                .sorted()
                                            val startedAt = remote?.startedTimestamp ?: local.startedTimestamp
                                            val checks = local.dailyChecks.map { localCheck ->
                                                val remoteCheck = remoteChecks[localCheck.dayNumber]?.check
                                                when {
                                                    remoteCheck?.completed == true -> remoteCheck
                                                    localCheck.completed -> localCheck
                                                    remoteCheck != null -> remoteCheck
                                                    else -> localCheck
                                                }
                                            }
                                            val status = when {
                                                completedDays.size >= local.targetDays -> ExperimentStatus.COMPLETED
                                                remote?.status == ExperimentStatus.ACTIVE ||
                                                    local.status == ExperimentStatus.ACTIVE ||
                                                    startedAt != null -> ExperimentStatus.ACTIVE
                                                else -> local.status
                                            }
                                            local.copy(
                                                status = status,
                                                startedTimestamp = startedAt,
                                                completedDays = completedDays,
                                                dailyChecks = checks
                                            )
                                        }

                                        val mergedGameScores = (snapshot.gameScores + localGameScores)
                                            .distinctBy { it.id }
                                            .sortedByDescending { it.timestamp }
                                        val mergedImpactLogs = (snapshot.impactLogs + localImpactLogs)
                                            .distinctBy { it.id }
                                            .sortedByDescending { it.timestamp }

                                        val mergedDna = snapshot.householdDna
                                            ?: if (localProfile.onboardingCompleted) {
                                                repository.householdDna.value
                                            } else {
                                                null
                                            }
                                        val mergedProfile = snapshot.profile.copy(
                                            displayName = localProfile.displayName.ifBlank {
                                                snapshot.profile.displayName
                                            },
                                            city = localProfile.city.ifBlank { snapshot.profile.city },
                                            onboardingCompleted = snapshot.profile.onboardingCompleted ||
                                                localProfile.onboardingCompleted,
                                            // Reward balances and avatar ownership are server-authoritative.
                                            selectedAvatar = snapshot.profile.selectedAvatar,
                                            unlockedAvatars = snapshot.profile.unlockedAvatars
                                        )

                                        repository.restoreCloudData(
                                            profile = mergedProfile,
                                            dna = mergedDna,
                                            footprints = mergedFootprints,
                                            experiments = mergedExperiments,
                                            gameScores = mergedGameScores,
                                            impactLogs = mergedImpactLogs
                                        )

                                        // One-time backfill: make locally retained user records durable in
                                        // Supabase before subsequent sign-outs or device changes.
                                        val syncFailures = mutableListOf<String>()
                                        suspend fun migrateRecord(label: String, action: suspend () -> Unit) {
                                            try {
                                                action()
                                            } catch (syncError: Exception) {
                                                syncFailures.add(
                                                    "$label: ${syncError.message ?: "sync failed"}"
                                                )
                                            }
                                        }

                                        migrateRecord("profile") {
                                            cloudRepository.saveProfile(mergedProfile)
                                        }
                                        if (mergedDna != null) {
                                            migrateRecord("household profile") {
                                                cloudRepository.saveHouseholdDna(
                                                    authenticatedUserId,
                                                    mergedDna
                                                )
                                            }
                                        }
                                        for (entry in localFootprints) {
                                            migrateRecord("electricity history") {
                                                cloudRepository.saveFootprint(authenticatedUserId, entry)
                                            }
                                        }
                                        for (experiment in mergedExperiments) {
                                            val hasCloudState = snapshot.experiments.any {
                                                it.id == experiment.id
                                            }
                                            val isRealProgress = experiment.startedTimestamp != null ||
                                                (experiment.completedDays.isNotEmpty() &&
                                                    experiment.id != "exp_ac_efficiency")
                                            if (hasCloudState || isRealProgress) {
                                                migrateRecord("experiment history") {
                                                    cloudRepository.saveExperiment(
                                                        authenticatedUserId,
                                                        experiment
                                                    )
                                                }
                                            }
                                        }
                                        for (score in localGameScores) {
                                            migrateRecord("game history") {
                                                cloudRepository.saveGameScore(authenticatedUserId, score)
                                            }
                                        }
                                        for (impact in localImpactLogs) {
                                            migrateRecord("impact history") {
                                                cloudRepository.saveImpactLog(authenticatedUserId, impact)
                                            }
                                        }

                                        cloudReadyUserId = authenticatedUserId
                                        _dataSyncStatus.value = if (syncFailures.isEmpty()) {
                                            "Cloud data synced"
                                        } else {
                                            "History kept on device; cloud migration incomplete: " +
                                                syncFailures.distinct().joinToString("; ")
                                        }
                                    } catch (error: Exception) {
                                        val activeProfile = repository.userProfile.value
                                        if (
                                            activeProfile.isSignedIn &&
                                            !activeProfile.isGuest &&
                                            activeProfile.id == authenticatedUserId
                                        ) {
                                            _dataSyncStatus.value =
                                                "Cloud load failed: ${error.message ?: "unknown error"}"
                                            _authUiState.value = AuthUiState(
                                                error = "You are signed in, but E50 could not connect to your cloud data. " +
                                                    "Check your internet connection and try signing in again. " +
                                                    "Details: ${error.message ?: "unknown error"}"
                                            )
                                        }
                                    } finally {
                                        if (cloudLoadingUserId == authenticatedUserId) {
                                            cloudLoadingUserId = null
                                        }
                                        val activeProfile = repository.userProfile.value
                                        if (
                                            activeProfile.isSignedIn &&
                                            !activeProfile.isGuest &&
                                            activeProfile.id == authenticatedUserId &&
                                            !_authUiState.value.isPasswordRecovery
                                        ) {
                                            if (cloudReadyUserId == authenticatedUserId) {
                                                navigateAfterCloudLoad()
                                            } else {
                                                navigationBackStack.clear()
                                                navigationBackStack.add(AppScreen.Auth)
                                                _currentScreen.value = AppScreen.Auth
                                            }
                                        }
                                    }
                                }
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

    private fun navigateAfterCloudLoad() {
        val destination = if (repository.userProfile.value.onboardingCompleted) {
            AppScreen.Dashboard
        } else {
            AppScreen.Onboarding
        }
        _authUiState.value = AuthUiState()
        navigationBackStack.clear()
        navigationBackStack.add(destination)
        _currentScreen.value = destination
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
                    redirectUrl = "com.aistudio.e50.ecoact.cloud://auth-callback/reset-password"
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
            if (cloudReadyUserId != userId) return@launch
            try {
                operation(userId)
                if (cloudReadyUserId == userId) {
                    _dataSyncStatus.value = "Saved $label to cloud"
                }
            } catch (error: Exception) {
                if (cloudReadyUserId == userId) {
                    _dataSyncStatus.value =
                        "Could not sync $label: ${error.message ?: "unknown error"}"
                }
            }
        }
    }

    private suspend fun refreshRewardProfile(userId: String) {
        val activeProfile = repository.userProfile.value
        val latest = cloudRepository.loadRewardProfile(userId, activeProfile.email)
        if (cloudReadyUserId == userId &&
            repository.userProfile.value.isSignedIn &&
            repository.userProfile.value.id == userId
        ) {
            repository.applyServerRewardProfile(latest)
        }
    }

    private fun syncProfile() {
        launchCloudSync("profile") { userId ->
            cloudRepository.saveProfile(repository.userProfile.value.copy(id = userId))
            refreshRewardProfile(userId)
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
            refreshRewardProfile(userId)
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

    fun skipOnboarding() {
        repository.skipOnboarding()
        syncProfile()
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
            refreshRewardProfile(userId)
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
            refreshRewardProfile(userId)
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
            refreshRewardProfile(userId)
        }
    }

    suspend fun unlockAvatar(avatar: String, @Suppress("UNUSED_PARAMETER") displayedCost: Int): Boolean {
        val userId = cloudReadyUserId ?: return false
        return try {
            cloudRepository.purchaseAvatar(avatar)
            refreshRewardProfile(userId)
            _dataSyncStatus.value = "Purchased avatar securely in cloud"
            true
        } catch (error: Exception) {
            _dataSyncStatus.value = "Avatar purchase failed: ${error.message ?: "unknown error"}"
            false
        }
    }

    suspend fun selectAvatar(avatar: String): Boolean {
        val userId = cloudReadyUserId ?: return false
        return try {
            cloudRepository.selectAvatar(avatar)
            refreshRewardProfile(userId)
            _dataSyncStatus.value = "Avatar selection saved to cloud"
            true
        } catch (error: Exception) {
            _dataSyncStatus.value = "Avatar selection failed: ${error.message ?: "unknown error"}"
            false
        }
    }

    fun resetData() {
        repository.resetToDemo()
        navigateTo(AppScreen.Dashboard)
    }
}
