package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class E50Repository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("e50_sustainability_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _householdDna = MutableStateFlow(HouseholdDna())
    val householdDna: StateFlow<HouseholdDna> = _householdDna.asStateFlow()

    private val _footprintEntries = MutableStateFlow<List<FootprintEntry>>(emptyList())
    val footprintEntries: StateFlow<List<FootprintEntry>> = _footprintEntries.asStateFlow()

    private val _experiments = MutableStateFlow<List<EnergyExperiment>>(emptyList())
    val experiments: StateFlow<List<EnergyExperiment>> = _experiments.asStateFlow()

    private val _communityBattles = MutableStateFlow<List<CommunityBattle>>(emptyList())
    val communityBattles: StateFlow<List<CommunityBattle>> = _communityBattles.asStateFlow()

    private val _cityLeaderboard = MutableStateFlow<List<CityLeaderboardEntry>>(emptyList())
    val cityLeaderboard: StateFlow<List<CityLeaderboardEntry>> = _cityLeaderboard.asStateFlow()

    private val _userLeaderboard = MutableStateFlow<List<UserLeaderboardEntry>>(emptyList())
    val userLeaderboard: StateFlow<List<UserLeaderboardEntry>> = _userLeaderboard.asStateFlow()

    private val _impactLogs = MutableStateFlow<List<ProofOfImpact>>(emptyList())
    val impactLogs: StateFlow<List<ProofOfImpact>> = _impactLogs.asStateFlow()

    private val _gameScores = MutableStateFlow<List<MiniGameResult>>(emptyList())
    val gameScores: StateFlow<List<MiniGameResult>> = _gameScores.asStateFlow()

    init {
        loadFromStorage()
    }

    // --- Deterministic DNA Recommendation Engine ---
    fun generateDnaRecommendations(dna: HouseholdDna): List<DnaRecommendation> {
        val list = mutableListOf<DnaRecommendation>()

        // Rule 1: AC usage
        if (dna.appliances.contains("Air Conditioner")) {
            if (dna.acTempPreference < 24) {
                list.add(
                    DnaRecommendation(
                        id = "rec_ac_temp",
                        title = "Shift AC to 24°C - 26°C Benchmark",
                        category = "Cooling Efficiency",
                        priority = "High",
                        reason = "Setting AC at ${dna.acTempPreference}°C consumes ~18-24% more electricity than the BEE recommended 24°C default setting.",
                        suggestedAction = "Run AC at 24°C with a ceiling fan at low speed for identical comfort and ~1.5 kWh/day reduction.",
                        potentialSavingKwh = (dna.acUsageHours * 0.25 * 30),
                        potentialSavingInr = (dna.acUsageHours * 0.25 * 30 * 7.5),
                        relatedExperimentId = "exp_ac_efficiency"
                    )
                )
            }
            if (dna.acUsageHours >= 6) {
                list.add(
                    DnaRecommendation(
                        id = "rec_ac_servicing",
                        title = "Bi-weekly AC Filter Rinsing",
                        category = "Maintenance",
                        priority = "Medium",
                        reason = "Dust buildup on indoor mesh forces compressor to run 15% longer in dusty semi-arid Gujarat climate.",
                        suggestedAction = "Wash mesh filters under tap water every 14 days to restore full airflow.",
                        potentialSavingKwh = 28.0,
                        potentialSavingInr = 210.0,
                        relatedExperimentId = "exp_cooling_habits"
                    )
                )
            }
        }

        // Rule 2: Water Heater (Geyser)
        if (dna.appliances.contains("Water Heater (Geyser)")) {
            list.add(
                DnaRecommendation(
                    id = "rec_geyser_timer",
                    title = "Limit Geyser Run to 15 Mins",
                    category = "Heating Optimization",
                    priority = "Medium",
                    reason = "Standard 2000W geysers heat storage water in 12-15 minutes; leaving them continuously switched on wastes 1-2 kWh in standing heat losses.",
                    suggestedAction = "Turn on geyser strictly 15 minutes before bath and switch off before entering bathroom.",
                    potentialSavingKwh = 35.0,
                    potentialSavingInr = 262.5
                )
            )
        }

        // Rule 3: High consumption baseline
        if (dna.monthlyKwhEstimate > 250) {
            list.add(
                DnaRecommendation(
                    id = "rec_standby_power",
                    title = "Eliminate Phantom / Standby Loads",
                    category = "Standby Power",
                    priority = "High",
                    reason = "Smart TVs, set-top boxes, and microwave displays draw 5-10W continuously in sleep mode, adding ~18 kWh unmetered monthly.",
                    suggestedAction = "Use master switch or spike guard with toggle switch to cut wall power entirely when asleep.",
                    potentialSavingKwh = 22.0,
                    potentialSavingInr = 165.0,
                    relatedExperimentId = "exp_standby_cutoff"
                )
            )
        }

        // Rule 4: Rooftop Solar Readiness
        if (dna.hasRooftopAccess && !dna.hasSolarInstalled) {
            val estimatedKwNeeded = (dna.monthlyKwhEstimate / 120.0).coerceIn(1.0, 5.0)
            list.add(
                DnaRecommendation(
                    id = "rec_solar_readiness",
                    title = "PM Surya Ghar: Rooftop Solar Potential",
                    category = "Rooftop Solar",
                    priority = "High",
                    reason = "You have dedicated rooftop access! Under Gujarat Surya Urja / PM Surya Ghar Muft Bijli Yojana, a ${String.format(Locale.US, "%.1f", estimatedKwNeeded)} kW system offsets up to 85% of your grid bill.",
                    suggestedAction = "Explore zero-export net metering through your local DISCOM (Torrent Power, UGVCL, DGVCL, MGVCL, PGVCL).",
                    potentialSavingKwh = dna.monthlyKwhEstimate * 0.75,
                    potentialSavingInr = dna.monthlyBillEstimate * 0.75
                )
            )
        } else if (!dna.hasRooftopAccess) {
            list.add(
                DnaRecommendation(
                    id = "rec_renter_friendly",
                    title = "Renter-Friendly Efficiency Upgrades",
                    category = "Tenant Habits",
                    priority = "Low",
                    reason = "As a resident without individual roof rights, focusing on zero-cost behavioral habits yields the greatest return.",
                    suggestedAction = "Utilize daylighting, natural cross-ventilation, and 5-star rated portable appliances.",
                    potentialSavingKwh = 30.0,
                    potentialSavingInr = 225.0,
                    relatedExperimentId = "exp_daylight"
                )
            )
        }

        // Rule 5: Daylighting
        list.add(
            DnaRecommendation(
                id = "rec_daylight_first",
                title = "Natural Daylighting (9 AM – 5 PM)",
                category = "Lighting Efficiency",
                priority = "Low",
                reason = "Drawing curtains open during daytime eliminates daytime artificial lighting in living and study areas.",
                suggestedAction = "Keep ambient windows unblocked during daytime study and chores.",
                potentialSavingKwh = 15.0,
                potentialSavingInr = 112.5,
                relatedExperimentId = "exp_daylight"
            )
        )

        return list
    }

    // --- Profile & DNA Updates ---
    fun updateProfile(name: String, city: String) {
        val updated = _userProfile.value.copy(displayName = name, city = city)
        _userProfile.value = updated
        saveToStorage()
    }

    fun loginUser(
        email: String,
        name: String,
        city: String,
        isGuest: Boolean,
        userId: String? = null
    ) {
        val current = _userProfile.value
        val isDifferentAccount = userId != null && userId != current.id

        val updated = current.copy(
            id = userId ?: current.id,
            email = if (email.isNotBlank()) email else current.email,
            displayName = if (name.isNotBlank()) name else current.displayName,
            city = if (city.isNotBlank()) city else current.city,
            isGuest = isGuest,
            isSignedIn = true,
            onboardingCompleted = if (isDifferentAccount) false else current.onboardingCompleted,
            ecoPoints = if (isDifferentAccount) 0 else current.ecoPoints,
            ecoCredits = if (isDifferentAccount) 0 else current.ecoCredits,
            selectedAvatar = if (isDifferentAccount) "🌱 Eco Pioneer" else current.selectedAvatar,
            unlockedAvatars = if (isDifferentAccount) listOf("🌱 Eco Pioneer") else current.unlockedAvatars
        )

        if (isDifferentAccount) {
            // These values are device-local until Phase 2. Never carry one account's
            // personal records over to another authenticated account.
            _householdDna.value = HouseholdDna()
            _footprintEntries.value = emptyList()
            _experiments.value = getDefaultExperiments()
            _communityBattles.value = emptyList()
            _cityLeaderboard.value = emptyList()
            _userLeaderboard.value = emptyList()
            _impactLogs.value = emptyList()
            _gameScores.value = emptyList()
        }

        _userProfile.value = updated
        saveToStorage()
    }

    fun signOutUser() {
        val updated = _userProfile.value.copy(isSignedIn = false)
        _userProfile.value = updated
        saveToStorage()
    }

    /**
     * Replace the active account's cached data with its Supabase snapshot.
     * This method intentionally performs no reward calculations.
     */
    fun restoreCloudData(
        profile: UserProfile,
        dna: HouseholdDna?,
        footprints: List<FootprintEntry>,
        experiments: List<EnergyExperiment>,
        gameScores: List<MiniGameResult>,
        impactLogs: List<ProofOfImpact>,
        communityBattles: List<CommunityBattle>,
        cityLeaderboard: List<CityLeaderboardEntry>,
        userLeaderboard: List<UserLeaderboardEntry>
    ) {
        _userProfile.value = profile.copy(isGuest = false, isSignedIn = true)
        _householdDna.value = dna ?: HouseholdDna()
        _footprintEntries.value = footprints
        _experiments.value = experiments
        _gameScores.value = gameScores
        _impactLogs.value = impactLogs
        _communityBattles.value = communityBattles
        _cityLeaderboard.value = cityLeaderboard
        _userLeaderboard.value = userLeaderboard
        checkAchievements()
        saveToStorage()
    }

    fun completeOnboarding(dna: HouseholdDna, name: String, city: String) {
        _householdDna.value = dna
        val current = _userProfile.value
        val updated = current.copy(
            displayName = name,
            city = city,
            onboardingCompleted = true,
            isSignedIn = true,
            ecoPoints = current.ecoPoints
        )
        _userProfile.value = updated
        checkAchievements()
        saveToStorage()
    }

    fun skipOnboarding() {
        _userProfile.value = _userProfile.value.copy(onboardingCompleted = true)
        saveToStorage()
    }

    fun updateHouseholdDna(dna: HouseholdDna) {
        _householdDna.value = dna
        saveToStorage()
    }

    // --- Footprint Tracker ---
    fun addFootprintEntry(entry: FootprintEntry) {
        val current = _footprintEntries.value.toMutableList()
        // Replace if month and year match or prepend
        val existingIndex = current.indexOfFirst { it.monthName == entry.monthName && it.year == entry.year }
        if (existingIndex >= 0) {
            current[existingIndex] = entry
        } else {
            current.add(0, entry)
        }
        _footprintEntries.value = current

        // Reward points are issued by the Supabase footprint-insert trigger.
        checkAchievements()
        saveToStorage()
    }

    fun updateFootprintEntry(entry: FootprintEntry) {
        val current = _footprintEntries.value.toMutableList()
        val index = current.indexOfFirst { it.id == entry.id }
        if (index >= 0) {
            current[index] = entry
            _footprintEntries.value = current
            saveToStorage()
        } else {
            addFootprintEntry(entry)
        }
    }

    fun deleteFootprintEntry(id: String) {
        val current = _footprintEntries.value.filterNot { it.id == id }
        _footprintEntries.value = current
        saveToStorage()
    }

    // --- Experiments ---
    fun startExperiment(experimentId: String) {
        val currentList = _experiments.value.map { exp ->
            if (exp.id == experimentId) {
                exp.copy(
                    status = ExperimentStatus.ACTIVE,
                    startedTimestamp = System.currentTimeMillis()
                )
            } else exp
        }
        _experiments.value = currentList
        saveToStorage()
    }

    fun recordDailyExperimentCheck(
        experimentId: String,
        dayNumber: Int,
        readingKwh: Double?,
        notes: String
    ): Boolean {
        var pointsAwarded = false
        val todayStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

        val currentList = _experiments.value.map { exp ->
            if (exp.id == experimentId) {
                val alreadyCompleted = exp.completedDays.contains(dayNumber)
                val newCompletedDays = if (alreadyCompleted) exp.completedDays else exp.completedDays + dayNumber

                val updatedChecks = exp.dailyChecks.map { check ->
                    if (check.dayNumber == dayNumber) {
                        check.copy(
                            completed = true,
                            dateString = todayStr,
                            readingKwh = readingKwh,
                            notes = notes
                        )
                    } else check
                }

                val allCompleted = newCompletedDays.size >= 7
                val isFirstTimeCompleted = allCompleted && exp.status != ExperimentStatus.COMPLETED
                val newStatus = if (allCompleted) ExperimentStatus.COMPLETED else exp.status

                if (!alreadyCompleted) {
                    pointsAwarded = true
                    // Supabase awards this completion and the experiment bonus transactionally.
                }

                exp.copy(
                    completedDays = newCompletedDays,
                    dailyChecks = updatedChecks,
                    status = newStatus
                )
            } else exp
        }

        _experiments.value = currentList
        checkAchievements()
        saveToStorage()
        return pointsAwarded
    }

    // --- Mini-Games ---
    fun recordGameScore(result: MiniGameResult) {
        if (_gameScores.value.any { it.id == result.id }) {
            return // Guard against duplicate submissions
        }
        val list = _gameScores.value.toMutableList()
        list.add(0, result)
        _gameScores.value = list

        // Supabase awards a fixed daily game reward; client-reported credits are never trusted.
        checkAchievements()
        saveToStorage()
    }

    // --- Community Battles ---
    fun joinCommunityBattle(battleId: String) {
        val existing = _communityBattles.value.firstOrNull { it.id == battleId }
        if (existing == null || existing.isJoined) {
            return // Prevent duplicate join points or participant inflation
        }
        val list = _communityBattles.value.map { b ->
            if (b.id == battleId) {
                b.copy(isJoined = true, participantsCount = b.participantsCount + 1)
            } else b
        }
        _communityBattles.value = list
        // Community battle rewards are disabled until Phase 4 persists verified contributions.
        checkAchievements()
        saveToStorage()
    }

    fun recordBattleContribution(battleId: String, amount: Double) {
        if (amount <= 0.0) return // Validate numerical amount
        val list = _communityBattles.value.map { b ->
            if (b.id == battleId) {
                val updatedCurrent = b.currentMetric + amount
                val userCont = b.userContribution + amount
                b.copy(currentMetric = updatedCurrent, userContribution = userCont)
            } else b
        }
        _communityBattles.value = list
        // Do not issue client-side points for self-reported community contributions.
        checkAchievements()
        saveToStorage()
    }

    // --- Proof of Impact ---
    fun logProofOfImpact(impact: ProofOfImpact) {
        if (impact.title.isBlank() || impact.estimatedKwhSaved <= 0.0) return
        val current = _impactLogs.value.toMutableList()
        current.add(0, impact)
        _impactLogs.value = current

        // Supabase applies a capped daily impact-submission reward.
        checkAchievements()
        saveToStorage()
    }

    // --- Rewards & Avatars ---
    fun applyServerRewardProfile(profile: UserProfile) {
        _userProfile.value = _userProfile.value.copy(
            ecoPoints = profile.ecoPoints,
            ecoCredits = profile.ecoCredits,
            selectedAvatar = profile.selectedAvatar,
            unlockedAvatars = profile.unlockedAvatars
        )
        checkAchievements()
        saveToStorage()
    }

    // --- Achievements Computation ---
    fun getAchievements(): List<AchievementItem> {
        val profile = _userProfile.value
        val exps = _experiments.value
        val games = _gameScores.value
        val impacts = _impactLogs.value
        val battles = _communityBattles.value

        val completedExpsCount = exps.count { it.status == ExperimentStatus.COMPLETED }
        val activeExp = exps.any { it.status == ExperimentStatus.ACTIVE || it.status == ExperimentStatus.COMPLETED }
        val hasEnergyDetective = games.any { it.gameType == "ENERGY_DETECTIVE" }
        val hasEcoSort = games.any { it.gameType == "ECO_SORT" }
        val hasCityPlanner = games.any { it.gameType == "GREEN_CITY" }
        val joinedBattle = battles.any { it.isJoined }

        return listOf(
            AchievementItem(
                id = "ach_first_step",
                title = "First Green Step",
                description = "Complete household DNA setup or log your first energy action.",
                iconName = "🌱",
                isUnlocked = profile.onboardingCompleted || impacts.isNotEmpty(),
                progress = if (profile.onboardingCompleted || impacts.isNotEmpty()) 1.0f else 0.5f,
                progressText = if (profile.onboardingCompleted) "Completed" else "In Progress"
            ),
            AchievementItem(
                id = "ach_7day_explorer",
                title = "Seven-Day Explorer",
                description = "Successfully complete any 7-Day Energy Experiment.",
                iconName = "⚡",
                isUnlocked = completedExpsCount >= 1,
                progress = (completedExpsCount.toFloat() / 1f).coerceAtMost(1f),
                progressText = "$completedExpsCount / 1 Challenge"
            ),
            AchievementItem(
                id = "ach_detective",
                title = "Energy Detective",
                description = "Investigate and locate hidden vampire power drains in Energy Detective.",
                iconName = "🔍",
                isUnlocked = hasEnergyDetective,
                progress = if (hasEnergyDetective) 1.0f else 0.0f,
                progressText = if (hasEnergyDetective) "Solved" else "Play Game"
            ),
            AchievementItem(
                id = "ach_sort_master",
                title = "Eco Sorting Expert",
                description = "Correctly segregate recyclables, compostables, and e-waste in Eco Sort.",
                iconName = "♻️",
                isUnlocked = hasEcoSort,
                progress = if (hasEcoSort) 1.0f else 0.0f,
                progressText = if (hasEcoSort) "Mastered" else "Play Game"
            ),
            AchievementItem(
                id = "ach_city_builder",
                title = "Green City Planner",
                description = "Design a low-carbon balanced municipality in Green City Builder.",
                iconName = "🏙️",
                isUnlocked = hasCityPlanner,
                progress = if (hasCityPlanner) 1.0f else 0.0f,
                progressText = if (hasCityPlanner) "Constructed" else "Play Game"
            ),
            AchievementItem(
                id = "ach_community_champ",
                title = "Community Champion",
                description = "Join a Mission LiFE collective climate battle and log your contribution.",
                iconName = "🏆",
                isUnlocked = joinedBattle,
                progress = if (joinedBattle) 1.0f else 0.0f,
                progressText = if (joinedBattle) "Joined" else "Explore Battles"
            )
        )
    }

    private fun checkAchievements() {
        // Triggers state refresh
        _userProfile.value = _userProfile.value.copy()
    }

    // Community views are populated only from authenticated Supabase RPC results.
    fun updateCommunityData(
        battles: List<CommunityBattle>,
        cities: List<CityLeaderboardEntry>,
        users: List<UserLeaderboardEntry>
    ) {
        _communityBattles.value = battles
        _cityLeaderboard.value = cities
        _userLeaderboard.value = users
        checkAchievements()
        saveToStorage()
    }

    fun setLeaderboardVisibility(visible: Boolean) {
        _userProfile.value = _userProfile.value.copy(leaderboardVisible = visible)
        saveToStorage()
    }

    // --- Persistence Implementation ---
    private fun loadFromStorage() {
        val isFirstLaunch = !prefs.contains("e50_initialized")
        if (isFirstLaunch) {
            seedDefaultData()
            saveToStorage()
            return
        }

        try {
            // User Profile
            val profileJson = prefs.getString("user_profile", null)
            if (profileJson != null) {
                val obj = JSONObject(profileJson)
                val unlockedArr = obj.optJSONArray("unlockedAvatars") ?: JSONArray()
                val unlockedList = mutableListOf<String>()
                for (i in 0 until unlockedArr.length()) {
                    unlockedList.add(unlockedArr.getString(i))
                }
                _userProfile.value = UserProfile(
                    id = obj.optString("id", "user_1"),
                    email = obj.optString("email", "citizen@e50.in"),
                    displayName = obj.optString("displayName", "Priya Sharma"),
                    city = obj.optString("city", "Ahmedabad"),
                    isGuest = obj.optBoolean("isGuest", false),
                    isSignedIn = obj.optBoolean("isSignedIn", true),
                    onboardingCompleted = obj.optBoolean("onboardingCompleted", true),
                    ecoPoints = obj.optInt("ecoPoints", 120),
                    ecoCredits = obj.optInt("ecoCredits", 350),
                    selectedAvatar = obj.optString("selectedAvatar", "🌱 Eco Pioneer"),
                    unlockedAvatars = if (unlockedList.isEmpty()) listOf("🌱 Eco Pioneer") else unlockedList,
                    leaderboardVisible = obj.optBoolean("leaderboardVisible", false)
                )
            }

            // Household DNA
            val dnaJson = prefs.getString("household_dna", null)
            if (dnaJson != null) {
                val obj = JSONObject(dnaJson)
                val appArr = obj.optJSONArray("appliances") ?: JSONArray()
                val appList = mutableListOf<String>()
                for (i in 0 until appArr.length()) appList.add(appArr.getString(i))

                val intArr = obj.optJSONArray("sustainabilityInterests") ?: JSONArray()
                val intList = mutableListOf<String>()
                for (i in 0 until intArr.length()) intList.add(intArr.getString(i))

                _householdDna.value = HouseholdDna(
                    householdSize = obj.optInt("householdSize", 4),
                    housingType = obj.optString("housingType", "Apartment"),
                    ownership = obj.optString("ownership", "Owner"),
                    monthlyKwhEstimate = obj.optDouble("monthlyKwhEstimate", 260.0),
                    monthlyBillEstimate = obj.optDouble("monthlyBillEstimate", 1850.0),
                    appliances = appList,
                    acUsageHours = obj.optInt("acUsageHours", 6),
                    acTempPreference = obj.optInt("acTempPreference", 22),
                    hasRooftopAccess = obj.optBoolean("hasRooftopAccess", true),
                    hasSolarInstalled = obj.optBoolean("hasSolarInstalled", false),
                    sustainabilityInterests = intList
                )
            }

            // Footprint Entries
            val footprintJson = prefs.getString("footprint_entries", null)
            if (footprintJson != null) {
                val arr = JSONArray(footprintJson)
                val entries = mutableListOf<FootprintEntry>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    entries.add(
                        FootprintEntry(
                            id = obj.getString("id"),
                            monthName = obj.getString("monthName"),
                            year = obj.getInt("year"),
                            kwh = obj.getDouble("kwh"),
                            billAmount = obj.getDouble("billAmount"),
                            notes = obj.optString("notes", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _footprintEntries.value = entries
            } else {
                _footprintEntries.value = getDefaultFootprintEntries()
            }

            // Experiments
            val expsJson = prefs.getString("experiments", null)
            if (expsJson != null) {
                val arr = JSONArray(expsJson)
                val expList = mutableListOf<EnergyExperiment>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val completedArr = obj.optJSONArray("completedDays") ?: JSONArray()
                    val completedDays = mutableListOf<Int>()
                    for (j in 0 until completedArr.length()) completedDays.add(completedArr.getInt(j))

                    val dailyChecksArr = obj.optJSONArray("dailyChecks") ?: JSONArray()
                    val checks = mutableListOf<DailyCheck>()
                    for (j in 0 until dailyChecksArr.length()) {
                        val cObj = dailyChecksArr.getJSONObject(j)
                        checks.add(
                            DailyCheck(
                                dayNumber = cObj.getInt("dayNumber"),
                                completed = cObj.getBoolean("completed"),
                                dateString = cObj.optString("dateString", ""),
                                readingKwh = if (cObj.has("readingKwh") && !cObj.isNull("readingKwh")) cObj.getDouble("readingKwh") else null,
                                notes = cObj.optString("notes", "")
                            )
                        )
                    }

                    val defaultExp = getDefaultExperiments().firstOrNull { it.id == obj.getString("id") }
                        ?: getDefaultExperiments().first()

                    expList.add(
                        defaultExp.copy(
                            status = ExperimentStatus.valueOf(obj.optString("status", "NOT_STARTED")),
                            startedTimestamp = if (obj.has("startedTimestamp")) obj.getLong("startedTimestamp") else null,
                            completedDays = completedDays,
                            dailyChecks = if (checks.isNotEmpty()) checks else defaultExp.dailyChecks
                        )
                    )
                }
                _experiments.value = expList
            } else {
                _experiments.value = getDefaultExperiments()
            }

            // Community Battles
            val battlesJson = prefs.getString("community_battles", null)
            if (battlesJson != null) {
                val arr = JSONArray(battlesJson)
                val battleList = mutableListOf<CommunityBattle>()
                val defaultBattles = getDefaultCommunityBattles()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.getString("id")
                    val defBattle = defaultBattles.firstOrNull { it.id == id }
                    battleList.add(
                        CommunityBattle(
                            id = id,
                            title = obj.optString("title", defBattle?.title ?: ""),
                            subtitle = obj.optString("subtitle", defBattle?.subtitle ?: ""),
                            region = obj.optString("region", defBattle?.region ?: "Gujarat Metro"),
                            targetGoal = obj.optString("targetGoal", defBattle?.targetGoal ?: ""),
                            currentMetric = obj.optDouble("currentMetric", defBattle?.currentMetric ?: 0.0),
                            targetMetric = obj.optDouble("targetMetric", defBattle?.targetMetric ?: 1000.0),
                            metricUnit = obj.optString("metricUnit", defBattle?.metricUnit ?: "Units"),
                            participantsCount = obj.optInt("participantsCount", defBattle?.participantsCount ?: 1),
                            daysLeft = obj.optInt("daysLeft", defBattle?.daysLeft ?: 5),
                            isJoined = obj.optBoolean("isJoined", false),
                            userContribution = obj.optDouble("userContribution", 0.0),
                            rewardPoints = obj.optInt("rewardPoints", defBattle?.rewardPoints ?: 100)
                        )
                    )
                }
                _communityBattles.value = if (battleList.isNotEmpty()) battleList else getDefaultCommunityBattles()
            } else {
                _communityBattles.value = getDefaultCommunityBattles()
            }

            // Game Scores
            val scoresJson = prefs.getString("game_scores", null)
            if (scoresJson != null) {
                val arr = JSONArray(scoresJson)
                val scoresList = mutableListOf<MiniGameResult>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    scoresList.add(
                        MiniGameResult(
                            id = obj.getString("id"),
                            gameType = obj.getString("gameType"),
                            score = obj.getInt("score"),
                            accuracyPercent = obj.optInt("accuracyPercent", 100),
                            ecoCreditsEarned = obj.optInt("ecoCreditsEarned", 50),
                            summary = obj.optString("summary", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                _gameScores.value = scoresList
            }

            // Impact Logs
            val impactJson = prefs.getString("impact_logs", null)
            if (impactJson != null) {
                val arr = JSONArray(impactJson)
                val logs = mutableListOf<ProofOfImpact>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    logs.add(
                        ProofOfImpact(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            category = obj.getString("category"),
                            actionDetail = obj.getString("actionDetail"),
                            estimatedKwhSaved = obj.getDouble("estimatedKwhSaved"),
                            estimatedKgCo2Avoided = obj.getDouble("estimatedKgCo2Avoided"),
                            photoNote = obj.optString("photoNote", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            status = obj.optString("status", "Verified & Persisted")
                        )
                    )
                }
                _impactLogs.value = logs
            } else {
                _impactLogs.value = getDefaultImpactLogs()
            }

        } catch (e: Exception) {
            seedDefaultData()
        }
    }

    private fun seedDefaultData() {
        _userProfile.value = UserProfile(
            id = "user_e50_local",
            email = "priya.sharma@gujarat.in",
            displayName = "Priya Sharma",
            city = "Ahmedabad",
            isGuest = false,
            onboardingCompleted = true,
            ecoPoints = 145,
            ecoCredits = 420,
            selectedAvatar = "🌱 Eco Pioneer",
            unlockedAvatars = listOf("🌱 Eco Pioneer", "⚡ Energy Sleuth")
        )
        _householdDna.value = HouseholdDna(
            householdSize = 4,
            housingType = "Apartment",
            ownership = "Owner",
            monthlyKwhEstimate = 265.0,
            monthlyBillEstimate = 1920.0,
            appliances = listOf("Air Conditioner", "Refrigerator", "Water Heater (Geyser)", "Ceiling Fans", "Washing Machine"),
            acUsageHours = 6,
            acTempPreference = 22,
            hasRooftopAccess = true,
            hasSolarInstalled = false,
            sustainabilityInterests = listOf("Energy Conservation", "Cooling Optimization", "Solar Energy")
        )
        _footprintEntries.value = getDefaultFootprintEntries()
        _experiments.value = getDefaultExperiments()
        _communityBattles.value = getDefaultCommunityBattles()
        _impactLogs.value = getDefaultImpactLogs()
        saveToStorage()
    }

    fun resetToDemo() {
        prefs.edit().clear().apply()
        seedDefaultData()
    }

    private fun saveToStorage() {
        try {
            val editor = prefs.edit()
            editor.putBoolean("e50_initialized", true)

            // User Profile
            val p = _userProfile.value
            val pObj = JSONObject().apply {
                put("id", p.id)
                put("email", p.email)
                put("displayName", p.displayName)
                put("city", p.city)
                put("isGuest", p.isGuest)
                put("isSignedIn", p.isSignedIn)
                put("onboardingCompleted", p.onboardingCompleted)
                put("ecoPoints", p.ecoPoints)
                put("ecoCredits", p.ecoCredits)
                put("selectedAvatar", p.selectedAvatar)
                val arr = JSONArray()
                p.unlockedAvatars.forEach { arr.put(it) }
                put("unlockedAvatars", arr)
                put("leaderboardVisible", p.leaderboardVisible)
            }
            editor.putString("user_profile", pObj.toString())

            // Household DNA
            val d = _householdDna.value
            val dObj = JSONObject().apply {
                put("householdSize", d.householdSize)
                put("housingType", d.housingType)
                put("ownership", d.ownership)
                put("monthlyKwhEstimate", d.monthlyKwhEstimate)
                put("monthlyBillEstimate", d.monthlyBillEstimate)
                val apps = JSONArray()
                d.appliances.forEach { apps.put(it) }
                put("appliances", apps)
                put("acUsageHours", d.acUsageHours)
                put("acTempPreference", d.acTempPreference)
                put("hasRooftopAccess", d.hasRooftopAccess)
                put("hasSolarInstalled", d.hasSolarInstalled)
                val ints = JSONArray()
                d.sustainabilityInterests.forEach { ints.put(it) }
                put("sustainabilityInterests", ints)
            }
            editor.putString("household_dna", dObj.toString())

            // Footprint
            val fArr = JSONArray()
            _footprintEntries.value.forEach { f ->
                val fObj = JSONObject().apply {
                    put("id", f.id)
                    put("monthName", f.monthName)
                    put("year", f.year)
                    put("kwh", f.kwh)
                    put("billAmount", f.billAmount)
                    put("notes", f.notes)
                    put("timestamp", f.timestamp)
                }
                fArr.put(fObj)
            }
            editor.putString("footprint_entries", fArr.toString())

            // Experiments
            val eArr = JSONArray()
            _experiments.value.forEach { exp ->
                val expObj = JSONObject().apply {
                    put("id", exp.id)
                    put("status", exp.status.name)
                    exp.startedTimestamp?.let { put("startedTimestamp", it) }
                    val compArr = JSONArray()
                    exp.completedDays.forEach { compArr.put(it) }
                    put("completedDays", compArr)

                    val checksArr = JSONArray()
                    exp.dailyChecks.forEach { c ->
                        val cObj = JSONObject().apply {
                            put("dayNumber", c.dayNumber)
                            put("completed", c.completed)
                            put("dateString", c.dateString)
                            c.readingKwh?.let { put("readingKwh", it) }
                            put("notes", c.notes)
                        }
                        checksArr.put(cObj)
                    }
                    put("dailyChecks", checksArr)
                }
                eArr.put(expObj)
            }
            editor.putString("experiments", eArr.toString())

            // Community Battles
            val bArr = JSONArray()
            _communityBattles.value.forEach { battle ->
                val bObj = JSONObject().apply {
                    put("id", battle.id)
                    put("title", battle.title)
                    put("subtitle", battle.subtitle)
                    put("region", battle.region)
                    put("targetGoal", battle.targetGoal)
                    put("currentMetric", battle.currentMetric)
                    put("targetMetric", battle.targetMetric)
                    put("metricUnit", battle.metricUnit)
                    put("participantsCount", battle.participantsCount)
                    put("daysLeft", battle.daysLeft)
                    put("isJoined", battle.isJoined)
                    put("userContribution", battle.userContribution)
                    put("rewardPoints", battle.rewardPoints)
                }
                bArr.put(bObj)
            }
            editor.putString("community_battles", bArr.toString())

            // Game Scores
            val gArr = JSONArray()
            _gameScores.value.forEach { score ->
                val gObj = JSONObject().apply {
                    put("id", score.id)
                    put("gameType", score.gameType)
                    put("score", score.score)
                    put("accuracyPercent", score.accuracyPercent)
                    put("ecoCreditsEarned", score.ecoCreditsEarned)
                    put("summary", score.summary)
                    put("timestamp", score.timestamp)
                }
                gArr.put(gObj)
            }
            editor.putString("game_scores", gArr.toString())

            // Impact Logs
            val iArr = JSONArray()
            _impactLogs.value.forEach { imp ->
                val iObj = JSONObject().apply {
                    put("id", imp.id)
                    put("title", imp.title)
                    put("category", imp.category)
                    put("actionDetail", imp.actionDetail)
                    put("estimatedKwhSaved", imp.estimatedKwhSaved)
                    put("estimatedKgCo2Avoided", imp.estimatedKgCo2Avoided)
                    put("photoNote", imp.photoNote)
                    put("timestamp", imp.timestamp)
                    put("status", imp.status)
                }
                iArr.put(iObj)
            }
            editor.putString("impact_logs", iArr.toString())

            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getDefaultFootprintEntries(): List<FootprintEntry> {
        return listOf(
            FootprintEntry(
                id = "fp_oct_2026",
                monthName = "October",
                year = 2026,
                kwh = 248.0,
                billAmount = 1780.0,
                notes = "Switched AC thermostat to 24°C and cleaned dust filters"
            ),
            FootprintEntry(
                id = "fp_sep_2026",
                monthName = "September",
                year = 2026,
                kwh = 275.0,
                billAmount = 1990.0,
                notes = "Peak late summer humidity; running fans with AC"
            ),
            FootprintEntry(
                id = "fp_aug_2026",
                monthName = "August",
                year = 2026,
                kwh = 292.0,
                billAmount = 2140.0,
                notes = "Monsoon baseline consumption"
            )
        )
    }

    private fun getDefaultExperiments(): List<EnergyExperiment> {
        return listOf(
            EnergyExperiment(
                id = "exp_ac_efficiency",
                title = "AC 24°C Comfort Experiment",
                subtitle = "Set thermostat to 24°C + ceiling fan tandem",
                category = "Cooling",
                iconName = "❄️",
                targetDays = 7,
                description = "Each 1°C increase in AC thermostat setting reduces electricity consumption by ~6% while maintaining thermal comfort according to BEE standards.",
                actionGuide = listOf(
                    "Set AC thermostat strictly between 24°C and 26°C",
                    "Run ceiling fan at speed 2 or 3 to distribute airflow",
                    "Ensure doors and balcony windows remain fully latched",
                    "Close thick window curtains during afternoon sun (12 PM - 4 PM)"
                ),
                baselineTip = "Record typical meter units or daily run hours before starting.",
                expectedSavingTip = "Save ~1.5 to 2.2 kWh per day (~45-60 kWh/month).",
                ecoPointsReward = 80,
                status = ExperimentStatus.NOT_STARTED
            ),
            EnergyExperiment(
                id = "exp_standby_cutoff",
                title = "Switch-Off Standby Vampire Loads",
                subtitle = "Cut wall power for entertainment & chargers",
                category = "Appliances",
                iconName = "🔌",
                targetDays = 7,
                description = "Appliances left in sleep mode or plugged-in chargers continuously sip 3-12 Watts each without anyone using them.",
                actionGuide = listOf(
                    "Switch off master wall plugs for TV, set-top box, soundbars at bedtime",
                    "Unplug phone & laptop chargers as soon as charging completes",
                    "Turn off microwave power outlet when not heating meals",
                    "Check workstation power strips before retiring for the night"
                ),
                baselineTip = "Count every device displaying a tiny red/green standby light in your home.",
                expectedSavingTip = "Eliminates ~15 to 25 kWh per month of phantom drain.",
                ecoPointsReward = 70,
                status = ExperimentStatus.NOT_STARTED
            ),
            EnergyExperiment(
                id = "exp_daylight",
                title = "Daylight Before Lights",
                subtitle = "Natural sun illumination between 9 AM and 5 PM",
                category = "Lighting",
                iconName = "☀️",
                targetDays = 7,
                description = "Harness abundant Gujarat natural sunlight! Keep tube lights and downlights switched off during clear daylight hours.",
                actionGuide = listOf(
                    "Pull open window drapes and blinds in main hall and bedrooms",
                    "Position work/study desks perpendicular to windows for glare-free light",
                    "Keep hallway and kitchen lights off when passing through during the day"
                ),
                baselineTip = "Notice how many lights are burning unnecessarily on bright mornings.",
                expectedSavingTip = "Saves ~0.8 kWh daily in multi-room homes.",
                ecoPointsReward = 60,
                status = ExperimentStatus.NOT_STARTED
            ),
            EnergyExperiment(
                id = "exp_cooling_habits",
                title = "Efficient Cooling & Ventilation",
                subtitle = "Breeze flushing & shading before turning on AC",
                category = "Ventilation",
                iconName = "🍃",
                targetDays = 7,
                description = "Flush hot trapped daytime indoor air by opening opposite windows during cool night and morning hours.",
                actionGuide = listOf(
                    "Open opposite windows between 6:00 AM – 7:30 AM to let cool fresh air in",
                    "Seal windows and drop sunshades by 9:00 AM before outdoor heat rises",
                    "Clean indoor AC dust mesh filters on Day 1 of this challenge"
                ),
                baselineTip = "Feel indoor vs outdoor air temperature before flipping the AC switch.",
                expectedSavingTip = "Delay AC usage by 2-3 hours daily during summer.",
                ecoPointsReward = 75,
                status = ExperimentStatus.NOT_STARTED
            ),
            EnergyExperiment(
                id = "exp_smart_lighting",
                title = "Smart 100% LED Habit",
                subtitle = "Zero idle lighting and LED transition",
                category = "Lighting",
                iconName = "💡",
                targetDays = 7,
                description = "Never let an unoccupied room burn electricity. Turn off lights upon exiting and replace older CFLs/bulbs with 9W LEDs.",
                actionGuide = listOf(
                    "Establish a household rule: last person leaving a room flips the switch",
                    "Inspect all bulbs in bathrooms, storerooms, and balconies",
                    "Keep exterior porch lights off during sleeping hours"
                ),
                baselineTip = "Do a 2-minute walk-through of your residence right now to inspect burning lights.",
                expectedSavingTip = "Saves ~10-18% of lighting consumption.",
                ecoPointsReward = 65,
                status = ExperimentStatus.NOT_STARTED
            ),
            EnergyExperiment(
                id = "exp_awareness",
                title = "7-Day Daily Meter Reading",
                subtitle = "Log your electricity meter at the same hour daily",
                category = "Awareness",
                iconName = "📊",
                targetDays = 7,
                description = "What gets measured gets managed! Recording your electric meter reading at 8:00 PM every night builds acute energy awareness.",
                actionGuide = listOf(
                    "Locate your electric meter box outside your flat/bungalow",
                    "Read the active kWh digit display at 8:00 PM sharp",
                    "Enter the reading into E50 daily check",
                    "Calculate daily variance and discuss with family members"
                ),
                baselineTip = "Snap a photo of the initial meter display on Day 1.",
                expectedSavingTip = "Awareness alone reduces household waste by 8-12%.",
                ecoPointsReward = 100,
                status = ExperimentStatus.NOT_STARTED
            )
        )
    }

    private fun getDefaultCommunityBattles(): List<CommunityBattle> {
        return listOf(
            CommunityBattle(
                id = "battle_ahmedabad_surat",
                title = "Mission LiFE: Ahmedabad vs Surat Standby Sunset",
                subtitle = "Cut vampire power simultaneously across Gujarat's two mega hubs",
                region = "Gujarat Metro",
                targetGoal = "Collectively turn off 100,000 idle appliance switches this week",
                currentMetric = 68420.0,
                targetMetric = 100000.0,
                metricUnit = "Switches Toggled Off",
                participantsCount = 4120,
                daysLeft = 3,
                isJoined = true,
                userContribution = 18.0,
                rewardPoints = 120
            ),
            CommunityBattle(
                id = "battle_cool_24",
                title = "Vadodara 24°C Thermostat Pledge",
                subtitle = "Save 250,000 units of peak grid electricity during heatwaves",
                region = "Central Gujarat",
                targetGoal = "Maintain AC thermostat strictly at 24°C or above for 10 consecutive days",
                currentMetric = 192500.0,
                targetMetric = 250000.0,
                metricUnit = "kWh Conserved",
                participantsCount = 2850,
                daysLeft = 5,
                isJoined = false,
                userContribution = 0.0,
                rewardPoints = 90
            ),
            CommunityBattle(
                id = "battle_solar_sprint",
                title = "GEDA Rooftop Solar Literacy Sprint",
                subtitle = "Assess 5,000 residential rooftops for solar potential in Saurashtra",
                region = "Rajkot & Bhavnagar",
                targetGoal = "Complete 5,000 household rooftop shadow assessments",
                currentMetric = 3890.0,
                targetMetric = 5000.0,
                metricUnit = "Roofs Assessed",
                participantsCount = 1430,
                daysLeft = 8,
                isJoined = false,
                userContribution = 0.0,
                rewardPoints = 150
            )
        )
    }

    private fun getDefaultImpactLogs(): List<ProofOfImpact> {
        return listOf(
            ProofOfImpact(
                id = "imp_1",
                title = "Air Conditioner Thermostat at 24°C",
                category = "Energy Saving",
                actionDetail = "Ran AC with ceiling fan tandem for 6 hours overnight",
                estimatedKwhSaved = 2.4,
                estimatedKgCo2Avoided = 1.72,
                photoNote = "Bedroom remote locked at 24°C Eco Mode",
                status = "Verified & Persisted"
            ),
            ProofOfImpact(
                id = "imp_2",
                title = "Home Entertainment Standby Cutoff",
                category = "Energy Saving",
                actionDetail = "Switched off smart TV, set-top box, and Wi-Fi spike guard before sleep",
                estimatedKwhSaved = 0.6,
                estimatedKgCo2Avoided = 0.43,
                photoNote = "Power strip master switch turned off",
                status = "Verified & Persisted"
            )
        )
    }
}
