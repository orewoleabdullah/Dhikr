package com.abdullah.dhikr.features.qibla.presentation

data class QiblaUiState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val qiblaBearing: Double = 0.0,
    val distanceToKaabaKm: Double = 0.0,
    val deviceAzimuth: Float = 0f,
    val needleAngle: Float = 0f,
    val isAlignedWithQibla: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isGpsEnabled: Boolean = false,
    val locationName: String = "Detecting Location...",
    val sensorAccuracy: Int = 3,
    val hasCompassSensor: Boolean = true
)
