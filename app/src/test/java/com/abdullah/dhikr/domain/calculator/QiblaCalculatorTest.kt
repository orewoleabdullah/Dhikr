package com.abdullah.dhikr.domain.calculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaCalculatorTest {

    @Test
    fun calculateQiblaBearing_fromMecca_returnsZeroOrNearZero() {
        val bearing = QiblaCalculator.calculateQiblaBearing(
            userLat = QiblaCalculator.KAABA_LATITUDE,
            userLng = QiblaCalculator.KAABA_LONGITUDE
        )
        assertEquals(0.0, bearing, 0.1)
    }

    @Test
    fun calculateQiblaBearing_fromMedina_returnsSouthishBearing() {
        val bearing = QiblaCalculator.calculateQiblaBearing(24.4672, 39.6112)
        assertTrue("Bearing from Medina should be roughly South", bearing in 170.0..180.0)
    }

    @Test
    fun calculateQiblaBearing_fromLondon_returnsSoutheast() {
        val bearing = QiblaCalculator.calculateQiblaBearing(51.5074, -0.1278)
        assertEquals(119.0, bearing, 2.0)
    }

    @Test
    fun calculateDistanceToKaabaKm_fromMecca_returnsZero() {
        val distance = QiblaCalculator.calculateDistanceToKaabaKm(
            userLat = QiblaCalculator.KAABA_LATITUDE,
            userLng = QiblaCalculator.KAABA_LONGITUDE
        )
        assertEquals(0.0, distance, 1.0)
    }

    @Test
    fun calculateDistanceToKaabaKm_fromLondon_returnsApprox4800km() {
        val distance = QiblaCalculator.calculateDistanceToKaabaKm(51.5074, -0.1278)
        assertTrue("Distance should be roughly 4800km", distance in 4700.0..4900.0)
    }

    @Test
    fun calculateNeedleAngle_whenAligned_returnsZero() {
        val azimuth = 119f
        val qiblaBearing = 119.0
        val needleAngle = QiblaCalculator.calculateNeedleAngle(azimuth, qiblaBearing)
        assertEquals(0f, needleAngle, 0.001f)
    }

    @Test
    fun calculateNeedleAngle_wrapsCorrectly() {
        val azimuth = 350f
        val qiblaBearing = 10.0
        val needleAngle = QiblaCalculator.calculateNeedleAngle(azimuth, qiblaBearing)
        assertEquals(20f, needleAngle, 0.001f)
    }
}
