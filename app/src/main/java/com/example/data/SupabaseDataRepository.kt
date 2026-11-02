package com.example.data

import com.example.data.model.*
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Cloud persistence for records owned by the currently authenticated Supabase user.
 *
 * This repository deliberately never writes profiles.eco_points or profiles.eco_credits.
 * Those balances are server-authoritative and will be handled by the Phase 3 reward ledger.
 */
class SupabaseDataRepository {

    private val client
        get() = SupabaseClientProvider.client

    suspend fun loadUserSnapshot(userId: String, email: String): SupabaseUserSnapshot {
        val profile = client.from("profiles")
            .select { filter { eq("id", userId) } }
            .decodeSingle<ProfileDbRow>()

        val household = client.from("household_dna")
            .select { filter { eq("user_id", userId) } }
            .decodeList<HouseholdDnaDbRow>()
            .firstOrNull()
            ?.toModel()

        val footprints = client.from("footprint_entries")
            .select { filter { eq("user_id", userId) } }
            .decodeList<FootprintDbRow>()
            .map { it.toModel() }
            .sortedByDescending { it.timestamp }

        val experiments = client.from("user_experiments")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ExperimentDbRow>()

        val dailyChecks = client.from("daily_checks")
            .select { filter { eq("user_id", userId) } }
            .decodeList<DailyCheckDbRow>()

        val games = client.from("game_scores")
            .select { filter { eq("user_id", userId) } }
            .decodeList<GameScoreDbRow>()
            .map { it.toModel() }
            .sortedByDescending { it.timestamp }

        val impacts = client.from("impact_logs")
            .select { filter { eq("user_id", userId) } }
            .decodeList<ImpactLogDbRow>()
            .map { it.toModel() }
            .sortedByDescending { it.timestamp }

        return SupabaseUserSnapshot(
            profile = profile.toModel(email),
            householdDna = household,
            footprints = footprints,
            experiments = experiments.map { it.toModel() },
            dailyChecks = dailyChecks.map { RemoteDailyCheck(it.experimentId, it.toModel()) },
            gameScores = games,
            impactLogs = impacts
        )
    }

    suspend fun saveProfile(profile: UserProfile) {
        // Auth trigger/backfill guarantees the row exists; update only allowed profile fields.
        client.from("profiles").update(
            ProfileWriteRow(
                displayName = profile.displayName,
                city = profile.city,
                onboardingCompleted = profile.onboardingCompleted,
                selectedAvatar = profile.selectedAvatar,
                unlockedAvatars = profile.unlockedAvatars
            )
        ) {
            filter {
                eq("id", profile.id)
            }
        }
    }

    suspend fun saveHouseholdDna(userId: String, dna: HouseholdDna) {
        client.from("household_dna").upsert(
            HouseholdDnaDbRow(
                userId = userId,
                householdSize = dna.householdSize,
                housingType = dna.housingType,
                ownership = dna.ownership,
                monthlyKwhEstimate = dna.monthlyKwhEstimate,
                monthlyBillEstimate = dna.monthlyBillEstimate,
                appliances = dna.appliances,
                acUsageHours = dna.acUsageHours,
                acTempPreference = dna.acTempPreference,
                hasRooftopAccess = dna.hasRooftopAccess,
                hasSolarInstalled = dna.hasSolarInstalled,
                sustainabilityInterests = dna.sustainabilityInterests
            )
        ) {
            onConflict = "user_id"
        }
    }

    suspend fun saveFootprint(userId: String, entry: FootprintEntry) {
        client.from("footprint_entries").upsert(
            FootprintDbRow(
                userId = userId,
                id = entry.id,
                monthName = entry.monthName,
                year = entry.year,
                kwh = entry.kwh,
                billAmount = entry.billAmount,
                notes = entry.notes,
                timestampMs = entry.timestamp
            )
        ) {
            onConflict = "user_id,month_name,year"
        }
    }

    suspend fun deleteFootprint(userId: String, entryId: String) {
        client.from("footprint_entries").delete {
            filter {
                eq("user_id", userId)
                eq("id", entryId)
            }
        }
    }

    suspend fun saveExperiment(userId: String, experiment: EnergyExperiment) {
        client.from("user_experiments").upsert(
            ExperimentDbRow(
                userId = userId,
                experimentId = experiment.id,
                status = experiment.status.name,
                startedTimestampMs = experiment.startedTimestamp,
                completedDays = experiment.completedDays
            )
        ) {
            onConflict = "user_id,experiment_id"
        }

        val checks = experiment.dailyChecks.map { check ->
            DailyCheckDbRow(
                userId = userId,
                experimentId = experiment.id,
                dayNumber = check.dayNumber,
                completed = check.completed,
                dateString = check.dateString,
                readingKwh = check.readingKwh,
                notes = check.notes
            )
        }
        if (checks.isNotEmpty()) {
            client.from("daily_checks").upsert(
                checks
            ) {
                onConflict = "user_id,experiment_id,day_number"
            }
        }
    }

    suspend fun saveGameScore(userId: String, result: MiniGameResult) {
        // Ignore duplicate IDs on retry. The reported credit amount is metadata only.
        client.from("game_scores").upsert(
            GameScoreDbRow(
                userId = userId,
                id = result.id,
                gameType = result.gameType,
                score = result.score,
                accuracyPercent = result.accuracyPercent,
                reportedEcoCredits = result.ecoCreditsEarned,
                summary = result.summary,
                timestampMs = result.timestamp
            )
        ) {
            onConflict = "user_id,id"
            ignoreDuplicates = true
        }
    }

    suspend fun saveImpactLog(userId: String, impact: ProofOfImpact) {
        // Ignore duplicate IDs on retry; the mobile client cannot update or verify this record.
        client.from("impact_logs").upsert(
            ImpactLogDbRow(
                userId = userId,
                id = impact.id,
                title = impact.title,
                category = impact.category,
                actionDetail = impact.actionDetail,
                estimatedKwhSaved = impact.estimatedKwhSaved,
                estimatedKgCo2Avoided = impact.estimatedKgCo2Avoided,
                photoNote = impact.photoNote,
                timestampMs = impact.timestamp,
                status = impact.status
            )
        ) {
            onConflict = "user_id,id"
            ignoreDuplicates = true
        }
    }
}

data class SupabaseUserSnapshot(
    val profile: UserProfile,
    val householdDna: HouseholdDna?,
    val footprints: List<FootprintEntry>,
    val experiments: List<RemoteExperimentState>,
    val dailyChecks: List<RemoteDailyCheck>,
    val gameScores: List<MiniGameResult>,
    val impactLogs: List<ProofOfImpact>
)

data class RemoteDailyCheck(
    val experimentId: String,
    val check: DailyCheck
)

data class RemoteExperimentState(
    val id: String,
    val status: ExperimentStatus,
    val startedTimestamp: Long?,
    val completedDays: List<Int>
)

private fun ProfileDbRow.toModel(email: String) = UserProfile(
    id = id,
    email = email,
    // Older profile rows may contain NULL values from the original schema.
    // Normalize them before constructing the non-null Android model.
    displayName = displayName?.takeIf { it.isNotBlank() }
        ?: email.substringBefore("@").ifBlank { "Eco Citizen" },
    city = city?.takeIf { it.isNotBlank() } ?: "Ahmedabad",
    isGuest = false,
    isSignedIn = true,
    onboardingCompleted = onboardingCompleted,
    ecoPoints = ecoPoints,
    ecoCredits = ecoCredits,
    selectedAvatar = selectedAvatar?.takeIf { it.isNotBlank() } ?: "🌱 Eco Pioneer",
    unlockedAvatars = unlockedAvatars.orEmpty().ifEmpty { listOf("🌱 Eco Pioneer") }
)

private fun HouseholdDnaDbRow.toModel() = HouseholdDna(
    householdSize = householdSize,
    housingType = housingType,
    ownership = ownership,
    monthlyKwhEstimate = monthlyKwhEstimate,
    monthlyBillEstimate = monthlyBillEstimate,
    appliances = appliances,
    acUsageHours = acUsageHours,
    acTempPreference = acTempPreference,
    hasRooftopAccess = hasRooftopAccess,
    hasSolarInstalled = hasSolarInstalled,
    sustainabilityInterests = sustainabilityInterests
)

private fun FootprintDbRow.toModel() = FootprintEntry(
    id = id,
    monthName = monthName,
    year = year,
    kwh = kwh,
    billAmount = billAmount,
    notes = notes,
    timestamp = timestampMs
)

private fun ExperimentDbRow.toModel() = RemoteExperimentState(
    id = experimentId,
    status = runCatching { ExperimentStatus.valueOf(status) }
        .getOrDefault(ExperimentStatus.NOT_STARTED),
    startedTimestamp = startedTimestampMs,
    completedDays = completedDays
)

private fun DailyCheckDbRow.toModel() = DailyCheck(
    dayNumber = dayNumber,
    completed = completed,
    dateString = dateString,
    readingKwh = readingKwh,
    notes = notes
)

private fun GameScoreDbRow.toModel() = MiniGameResult(
    id = id,
    gameType = gameType,
    score = score,
    accuracyPercent = accuracyPercent,
    ecoCreditsEarned = reportedEcoCredits,
    summary = summary,
    timestamp = timestampMs
)

private fun ImpactLogDbRow.toModel() = ProofOfImpact(
    id = id,
    title = title,
    category = category,
    actionDetail = actionDetail,
    estimatedKwhSaved = estimatedKwhSaved,
    estimatedKgCo2Avoided = estimatedKgCo2Avoided,
    photoNote = photoNote,
    timestamp = timestampMs,
    status = status
)

@Serializable
private data class ProfileDbRow(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val city: String? = null,
    @SerialName("onboarding_completed") val onboardingCompleted: Boolean = false,
    @SerialName("selected_avatar") val selectedAvatar: String? = null,
    @SerialName("unlocked_avatars") val unlockedAvatars: List<String>? = null,
    @SerialName("eco_points") val ecoPoints: Int = 0,
    @SerialName("eco_credits") val ecoCredits: Int = 0
)

@Serializable
private data class ProfileWriteRow(
    @SerialName("display_name") val displayName: String,
    val city: String,
    @SerialName("onboarding_completed") val onboardingCompleted: Boolean,
    @SerialName("selected_avatar") val selectedAvatar: String,
    @SerialName("unlocked_avatars") val unlockedAvatars: List<String>
)

@Serializable
private data class HouseholdDnaDbRow(
    @SerialName("user_id") val userId: String,
    @SerialName("household_size") val householdSize: Int,
    @SerialName("housing_type") val housingType: String,
    val ownership: String,
    @SerialName("monthly_kwh_estimate") val monthlyKwhEstimate: Double,
    @SerialName("monthly_bill_estimate") val monthlyBillEstimate: Double,
    val appliances: List<String>,
    @SerialName("ac_usage_hours") val acUsageHours: Int,
    @SerialName("ac_temp_preference") val acTempPreference: Int,
    @SerialName("has_rooftop_access") val hasRooftopAccess: Boolean,
    @SerialName("has_solar_installed") val hasSolarInstalled: Boolean,
    @SerialName("sustainability_interests") val sustainabilityInterests: List<String>
)

@Serializable
private data class FootprintDbRow(
    @SerialName("user_id") val userId: String,
    val id: String,
    @SerialName("month_name") val monthName: String,
    val year: Int,
    val kwh: Double,
    @SerialName("bill_amount") val billAmount: Double,
    val notes: String,
    @SerialName("timestamp_ms") val timestampMs: Long
)

@Serializable
private data class ExperimentDbRow(
    @SerialName("user_id") val userId: String,
    @SerialName("experiment_id") val experimentId: String,
    val status: String,
    @SerialName("started_timestamp_ms") val startedTimestampMs: Long? = null,
    @SerialName("completed_days") val completedDays: List<Int> = emptyList()
)

@Serializable
private data class DailyCheckDbRow(
    @SerialName("user_id") val userId: String,
    @SerialName("experiment_id") val experimentId: String,
    @SerialName("day_number") val dayNumber: Int,
    val completed: Boolean,
    @SerialName("date_string") val dateString: String,
    @SerialName("reading_kwh") val readingKwh: Double? = null,
    val notes: String
)

@Serializable
private data class GameScoreDbRow(
    @SerialName("user_id") val userId: String,
    val id: String,
    @SerialName("game_type") val gameType: String,
    val score: Int,
    @SerialName("accuracy_percent") val accuracyPercent: Int,
    @SerialName("reported_eco_credits") val reportedEcoCredits: Int,
    val summary: String,
    @SerialName("timestamp_ms") val timestampMs: Long
)

@Serializable
private data class ImpactLogDbRow(
    @SerialName("user_id") val userId: String,
    val id: String,
    val title: String,
    val category: String,
    @SerialName("action_detail") val actionDetail: String,
    @SerialName("estimated_kwh_saved") val estimatedKwhSaved: Double,
    @SerialName("estimated_kg_co2_avoided") val estimatedKgCo2Avoided: Double,
    @SerialName("photo_note") val photoNote: String,
    @SerialName("timestamp_ms") val timestampMs: Long,
    val status: String
)
