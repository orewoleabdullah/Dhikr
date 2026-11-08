package com.abdullah.dhikr.domain.calculator

import kotlin.math.*

/**
 * Pure mathematical utility for calculating Qibla bearing relative to True North
 * and Haversine great-circle distance to the Holy Kaaba in Mecca.
 */
object QiblaCalculator {

    // Kaaba Coordinates in Mecca, Saudi Arabia
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the Qibla bearing in degrees (0..360) clockwise from True North
     * given the user's latitude and longitude.
     */
    fun calculateQiblaBearing(userLat: Double, userLng: Double): Double {
        if (abs(userLat - KAABA_LATITUDE) < 0.00001 && abs(userLng - KAABA_LONGITUDE) < 0.00001) {
            return 0.0
        }

        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val kaabaLatRad = Math.toRadians(KAABA_LATITUDE)
        val kaabaLngRad = Math.toRadians(KAABA_LONGITUDE)

        val deltaLng = kaabaLngRad - userLngRad

        val y = sin(deltaLng)
        val x = cos(userLatRad) * tan(kaabaLatRad) - sin(userLatRad) * cos(deltaLng)

        val qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)

        if (qiblaDeg < 0) {
            qiblaDeg += 360.0
        }

        return qiblaDeg
    }

    /**
     * Calculates the Haversine distance in kilometers from the user's location to the Kaaba.
     */
    fun calculateDistanceToKaabaKm(userLat: Double, userLng: Double): Double {
        val dLat = Math.toRadians(KAABA_LATITUDE - userLat)
        val dLng = Math.toRadians(KAABA_LONGITUDE - userLng)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(KAABA_LATITUDE)) *
                sin(dLng / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Computes the needle offset angle in degrees (0..360) given device azimuth and Qibla bearing.
     */
    fun calculateNeedleAngle(deviceAzimuth: Float, qiblaBearing: Double): Float {
        var angle = (qiblaBearing.toFloat() - deviceAzimuth) % 360f
        if (angle < 0) {
            angle += 360f
        }
        return angle
    }
}
