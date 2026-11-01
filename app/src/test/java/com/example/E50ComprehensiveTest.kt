package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.E50Repository
import com.example.data.model.FootprintEntry
import com.example.data.model.MiniGameResult
import com.example.data.model.ProofOfImpact
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class E50ComprehensiveTest {

    private lateinit var context: Context
    private lateinit var repo: E50Repository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        repo = E50Repository(context)
        repo.resetToDemo()
    }

    @Test
    fun testEcoPointsAndEcoCreditsStrictSeparation() {
        val initialPoints = repo.userProfile.value.ecoPoints
        val initialCredits = repo.userProfile.value.ecoCredits

        // 1. Mini-Game Result awards EcoCredits ONLY, NOT EcoPoints
        val gameResult = MiniGameResult(
            id = "test_game_1",
            gameType = "ENERGY_DETECTIVE",
            score = 80,
            accuracyPercent = 80,
            ecoCreditsEarned = 160,
            summary = "Detective test"
        )
        repo.recordGameScore(gameResult)

        assertEquals("EcoPoints must remain unchanged by mini-game scores", initialPoints, repo.userProfile.value.ecoPoints)
        assertEquals("EcoCredits must increase by game reward", initialCredits + 160, repo.userProfile.value.ecoCredits)

        // 2. Real-world Proof of Impact awards EcoPoints ONLY, NOT EcoCredits
        val currentCredits = repo.userProfile.value.ecoCredits
        val proof = ProofOfImpact(
            id = "test_proof_1",
            title = "LED Retrofit",
            category = "Lighting",
            actionDetail = "Replaced 5 incandescent bulbs with 9W LEDs",
            estimatedKwhSaved = 15.0,
            estimatedKgCo2Avoided = 10.74,
            photoNote = "Verified receipt"
        )
        repo.logProofOfImpact(proof)

        assertTrue("EcoPoints must increase by real-world impact action", repo.userProfile.value.ecoPoints > initialPoints)
        assertEquals("EcoCredits must NOT increase from real-world proof of impact", currentCredits, repo.userProfile.value.ecoCredits)
    }

    @Test
    fun testDuplicateRewardPreventionForGames() {
        val initialCredits = repo.userProfile.value.ecoCredits
        val gameResult = MiniGameResult(
            id = "fixed_game_id_100",
            gameType = "ECO_SORT",
            score = 100,
            accuracyPercent = 100,
            ecoCreditsEarned = 75,
            summary = "Sorted all waste correctly"
        )

        // First submission
        repo.recordGameScore(gameResult)
        assertEquals(initialCredits + 75, repo.userProfile.value.ecoCredits)

        // Duplicate submission with same result ID
        repo.recordGameScore(gameResult)
        assertEquals("Duplicate game score submissions must not re-award EcoCredits", initialCredits + 75, repo.userProfile.value.ecoCredits)
        assertEquals(1, repo.gameScores.value.count { it.id == "fixed_game_id_100" })
    }

    @Test
    fun testDuplicateRewardPreventionForCommunityBattles() {
        val targetBattle = repo.communityBattles.value.first { !it.isJoined }
        val initialPoints = repo.userProfile.value.ecoPoints
        val initialParticipants = targetBattle.participantsCount

        // First join
        repo.joinCommunityBattle(targetBattle.id)
        val afterFirstJoinPoints = repo.userProfile.value.ecoPoints
        assertEquals("Joining battle awards 20 EcoPoints", initialPoints + 20, afterFirstJoinPoints)

        val updatedBattle = repo.communityBattles.value.first { it.id == targetBattle.id }
        assertTrue(updatedBattle.isJoined)
        assertEquals(initialParticipants + 1, updatedBattle.participantsCount)

        // Attempt second join
        repo.joinCommunityBattle(targetBattle.id)
        assertEquals("Duplicate join must not re-award EcoPoints", afterFirstJoinPoints, repo.userProfile.value.ecoPoints)
        assertEquals("Duplicate join must not inflate participant count", initialParticipants + 1, repo.communityBattles.value.first { it.id == targetBattle.id }.participantsCount)
    }

    @Test
    fun testSessionPersistenceAcrossRestarts() {
        // Sign in user
        repo.loginUser("audit.tester@geda.gov.in", "Audit Officer", "Gandhinagar", false)
        assertTrue(repo.userProfile.value.isSignedIn)
        assertEquals("Audit Officer", repo.userProfile.value.displayName)
        assertEquals("Gandhinagar", repo.userProfile.value.city)

        // Simulate app restart with a fresh repository instance
        val freshRepo = E50Repository(context)
        assertTrue("Session state must persist as signed in across restarts", freshRepo.userProfile.value.isSignedIn)
        assertEquals("Audit Officer", freshRepo.userProfile.value.displayName)
        assertEquals("Gandhinagar", freshRepo.userProfile.value.city)

        // Sign out
        freshRepo.signOutUser()
        assertFalse(freshRepo.userProfile.value.isSignedIn)

        // Simulate another restart
        val signedOutRepo = E50Repository(context)
        assertFalse("Session state must persist as signed out across restarts", signedOutRepo.userProfile.value.isSignedIn)
    }

    @Test
    fun testFootprintEditingAndCeaFactorCalculations() {
        val newEntry = FootprintEntry(
            id = "fp_audit_test",
            monthName = "November",
            year = 2026,
            kwh = 200.0,
            billAmount = 1450.0,
            notes = "Initial reading"
        )
        repo.addFootprintEntry(newEntry)

        val added = repo.footprintEntries.value.first { it.id == "fp_audit_test" }
        assertEquals(200.0, added.kwh, 0.001)
        assertEquals(143.2, added.estimatedKgCo2e, 0.05) // 200 * 0.716

        // Now edit the entry
        val updatedEntry = added.copy(kwh = 180.0, notes = "Corrected reading from DISCOM bill")
        repo.updateFootprintEntry(updatedEntry)

        val retrieved = repo.footprintEntries.value.first { it.id == "fp_audit_test" }
        assertEquals(180.0, retrieved.kwh, 0.001)
        assertEquals(180.0 * 0.716, retrieved.estimatedKgCo2e, 0.05)
        assertEquals("Corrected reading from DISCOM bill", retrieved.notes)
    }

    @Test
    fun testAvatarPurchaseValidation() {
        val initialCredits = repo.userProfile.value.ecoCredits
        val affordableCost = 100
        val expensiveCost = 999999

        // Valid purchase
        val success = repo.unlockAvatar("⚡ Watt Sleuth", affordableCost)
        assertTrue(success)
        assertEquals(initialCredits - affordableCost, repo.userProfile.value.ecoCredits)
        assertTrue(repo.userProfile.value.unlockedAvatars.contains("⚡ Watt Sleuth"))

        // Invalid purchase with insufficient credits
        val fail = repo.unlockAvatar("🚀 Solar Commander", expensiveCost)
        assertFalse(fail)
        assertFalse(repo.userProfile.value.unlockedAvatars.contains("🚀 Solar Commander"))
    }
}
