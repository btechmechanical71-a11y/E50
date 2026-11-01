package com.example

import com.example.data.model.FootprintEntry
import com.example.data.model.HouseholdDna
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testIndianGridEmissionCalculation() {
        val entry = FootprintEntry(
            id = "test_1",
            monthName = "October",
            year = 2026,
            kwh = 100.0,
            billAmount = 750.0
        )
        // CEA v19 Indian National Grid weighted average is 0.716 kg CO2e / kWh
        assertEquals(71.6, entry.estimatedKgCo2e, 0.001)
    }

    @Test
    fun testHouseholdDnaSolarReadiness() {
        val dnaWithRoof = HouseholdDna(
            hasRooftopAccess = true,
            hasSolarInstalled = false,
            monthlyKwhEstimate = 240.0
        )
        val estimatedSystemKw = (dnaWithRoof.monthlyKwhEstimate / 120.0).coerceIn(1.0, 5.0)
        assertEquals(2.0, estimatedSystemKw, 0.01)
    }
}
