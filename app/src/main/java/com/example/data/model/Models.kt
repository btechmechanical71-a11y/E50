package com.example.data.model

data class UserProfile(
    val id: String = "user_local_1",
    val email: String = "citizen@e50.in",
    val displayName: String = "Priya Sharma",
    val city: String = "Ahmedabad",
    val isGuest: Boolean = false,
    val isSignedIn: Boolean = true,
    val onboardingCompleted: Boolean = true,
    val ecoPoints: Int = 120,
    val ecoCredits: Int = 350,
    val selectedAvatar: String = "🌱 Eco Pioneer",
    val unlockedAvatars: List<String> = listOf("🌱 Eco Pioneer", "⚡ Energy Sleuth", "🍃 Green Champion")
)

data class HouseholdDna(
    val householdSize: Int = 4,
    val housingType: String = "Apartment", // Apartment, Independent Villa, Row House, Tenement
    val ownership: String = "Owner", // Owner, Tenant / Rental
    val monthlyKwhEstimate: Double = 260.0,
    val monthlyBillEstimate: Double = 1850.0,
    val appliances: List<String> = listOf("Air Conditioner", "Refrigerator", "Water Heater (Geyser)", "Ceiling Fans", "Washing Machine"),
    val acUsageHours: Int = 6,
    val acTempPreference: Int = 22,
    val hasRooftopAccess: Boolean = true,
    val hasSolarInstalled: Boolean = false,
    val sustainabilityInterests: List<String> = listOf("Energy Conservation", "Cooling Optimization", "Solar Energy")
)

data class DnaRecommendation(
    val id: String,
    val title: String,
    val category: String,
    val priority: String, // High, Medium, Low
    val reason: String,
    val suggestedAction: String,
    val potentialSavingKwh: Double,
    val potentialSavingInr: Double,
    val relatedExperimentId: String? = null
)

data class FootprintEntry(
    val id: String,
    val monthName: String,
    val year: Int,
    val kwh: Double,
    val billAmount: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    // Official Central Electricity Authority (CEA) CO2 Baseline Database v19 (2023-24):
    // Weighted Average Emission Rate for Indian National Grid = 0.716 kg CO2e / kWh
    val estimatedKgCo2e: Double
        get() = kwh * 0.716
}

data class DailyCheck(
    val dayNumber: Int,
    val completed: Boolean = false,
    val dateString: String = "",
    val readingKwh: Double? = null,
    val notes: String = ""
)

data class EnergyExperiment(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val iconName: String,
    val targetDays: Int = 7,
    val description: String,
    val actionGuide: List<String>,
    val baselineTip: String,
    val expectedSavingTip: String,
    val ecoPointsReward: Int,
    val status: ExperimentStatus = ExperimentStatus.NOT_STARTED,
    val startedTimestamp: Long? = null,
    val completedDays: List<Int> = emptyList(),
    val dailyChecks: List<DailyCheck> = (1..7).map { DailyCheck(dayNumber = it) }
)

enum class ExperimentStatus {
    NOT_STARTED,
    ACTIVE,
    COMPLETED
}

data class MiniGameResult(
    val id: String,
    val gameType: String,
    val score: Int,
    val accuracyPercent: Int,
    val ecoCreditsEarned: Int,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CommunityBattle(
    val id: String,
    val title: String,
    val subtitle: String,
    val region: String, // e.g. "Gujarat State", "Ahmedabad vs Surat", "Vadodara District"
    val targetGoal: String,
    val currentMetric: Double,
    val targetMetric: Double,
    val metricUnit: String,
    val participantsCount: Int,
    val daysLeft: Int,
    val isJoined: Boolean = false,
    val userContribution: Double = 0.0,
    val rewardPoints: Int = 100
)

data class CityLeaderboardEntry(
    val rank: Int,
    val cityName: String,
    val state: String = "Gujarat",
    val activeCitizens: Int,
    val totalKwhSaved: Double,
    val ecoPointsTotal: Int,
    val isUserCity: Boolean = false
)

data class UserLeaderboardEntry(
    val rank: Int,
    val name: String,
    val city: String,
    val avatar: String,
    val ecoPoints: Int,
    val actionsCount: Int,
    val isCurrentUser: Boolean = false
)

data class ProofOfImpact(
    val id: String,
    val title: String,
    val category: String, // Energy Saving, Solar Adoption, Waste Segregation, Tree Care, Cool Roof
    val actionDetail: String,
    val estimatedKwhSaved: Double,
    val estimatedKgCo2Avoided: Double,
    val photoNote: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Verified & Persisted"
)

data class AchievementItem(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val isUnlocked: Boolean,
    val progress: Float, // 0.0f to 1.0f
    val progressText: String,
    val unlockedDate: String? = null
)
