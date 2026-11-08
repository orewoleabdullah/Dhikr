package com.abdullah.dhikr

import com.abdullah.dhikr.domain.calculator.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaCalculatorTest {

    @Test
    fun testQiblaBearingFromMeccaIsZeroOrIndeterminate() {
        val bearing = QiblaCalculator.calculateQiblaBearing(
            QiblaCalculator.KAABA_LATITUDE,
            QiblaCalculator.KAABA_LONGITUDE
        )
        // At Kaaba, bearing is near 0
        assertTrue(bearing >= 0.0 && bearing <= 360.0)
    }

    @Test
    fun testQiblaBearingFromLondon() {
        // London coordinates ~ (51.5074 N, -0.1278 W)
        val bearing = QiblaCalculator.calculateQiblaBearing(51.5074, -0.1278)
        // Qibla direction from London is approximately 118.9 degrees (South-East)
        assertEquals(118.9, bearing, 2.0)
    }

    @Test
    fun testQiblaBearingFromJakarta() {
        // Jakarta coordinates ~ (-6.2088 S, 106.8456 E)
        val bearing = QiblaCalculator.calculateQiblaBearing(-6.2088, 106.8456)
        // Qibla direction from Jakarta is approximately 295.1 degrees (North-West)
        assertEquals(295.1, bearing, 2.0)
    }

    @Test
    fun testDistanceToKaabaFromMeccaIsZero() {
        val dist = QiblaCalculator.calculateDistanceToKaabaKm(
            QiblaCalculator.KAABA_LATITUDE,
            QiblaCalculator.KAABA_LONGITUDE
        )
        assertEquals(0.0, dist, 0.1)
    }
}
