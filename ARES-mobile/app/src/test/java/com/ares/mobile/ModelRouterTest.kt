package com.ares.mobile

import com.ares.mobile.ai.AresModelVariant
import com.ares.mobile.ai.ModelRouter
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelRouterTest {
    @Test
    fun selectsE2BWhenAvailableRamIsBelowThreshold() {
        val result = ModelRouter.chooseAutomaticVariant(
            totalRamBytes = 4_000_000_000L,
            availableRamBytes = 3_000_000_000L,
        )

        assertEquals(AresModelVariant.E2B, result)
    }

    @Test
    fun selectsE4BWhenAvailableRamIsHighEnough() {
        val result = ModelRouter.chooseAutomaticVariant(
            totalRamBytes = 8_000_000_000L,
            availableRamBytes = 4_500_000_000L,
        )

        assertEquals(AresModelVariant.E4B, result)
    }

    @Test
    fun selectsE4BWhenTotalRamIsHighEvenIfFreeRamDrops() {
        val result = ModelRouter.chooseAutomaticVariant(
            totalRamBytes = 9_000_000_000L,
            availableRamBytes = 2_000_000_000L,
        )

        assertEquals(AresModelVariant.E4B, result)
    }
}