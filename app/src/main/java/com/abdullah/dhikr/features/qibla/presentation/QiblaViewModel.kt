package com.abdullah.dhikr.features.qibla.presentation

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import com.abdullah.dhikr.domain.calculator.QiblaCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

class QiblaViewModel(
    application: Application
) : AndroidViewModel(application), SensorEventListener, LocationListener {

    private val _uiState = MutableStateFlow(QiblaUiState())
    val uiState: StateFlow<QiblaUiState> = _uiState.asStateFlow()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    private var rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private var accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var magnetometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var smoothedAzimuth = 0f

    init {
        val hasCompass = rotationVectorSensor != null || (accelerometer != null && magnetometer != null)
        _uiState.update { it.copy(hasCompassSensor = hasCompass) }
    }

    fun startSensors() {
        if (rotationVectorSensor != null) {
            sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magnetometer?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }

        if (_uiState.value.hasLocationPermission) {
            requestLocationUpdatesInternal()
        }
    }

    fun stopSensors() {
        sensorManager?.unregisterListener(this)
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun updateLocationPermission(hasPermission: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = hasPermission) }

        if (hasPermission) {
            requestLocationUpdatesInternal()
        } else {
            stopLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshLocation() {
        if (_uiState.value.hasLocationPermission) {
            requestLocationUpdatesInternal()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdatesInternal() {
        if (locationManager == null) return

        val isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        _uiState.update { it.copy(isGpsEnabled = isGpsOn) }

        try {
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLast = lastGps ?: lastNet

            if (bestLast != null) {
                processLocationUpdate(bestLast)
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 10f, this)
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 10f, this)
            }
        } catch (_: Exception) {}
    }

    private fun processLocationUpdate(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val bearing = QiblaCalculator.calculateQiblaBearing(lat, lng)
        val distKm = QiblaCalculator.calculateDistanceToKaabaKm(lat, lng)

        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lng,
                qiblaBearing = bearing,
                distanceToKaabaKm = distKm,
                locationName = String.format(Locale.getDefault(), "%.2f° N, %.2f° E", lat, lng)
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuthRad = orientation[0]
            var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
            if (azimuthDeg < 0) azimuthDeg += 360f

            updateAzimuth(azimuthDeg)
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, gravity, 0, 3)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            }

            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                val azimuthRad = orientation[0]
                var azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()
                if (azimuthDeg < 0) azimuthDeg += 360f

                updateAzimuth(azimuthDeg)
            }
        }
    }

    private fun updateAzimuth(azimuthDeg: Float) {
        val diff = (azimuthDeg - smoothedAzimuth + 540f) % 360f - 180f
        smoothedAzimuth = (smoothedAzimuth + diff * 0.15f + 360f) % 360f

        val currentBearing = _uiState.value.qiblaBearing
        val needleAngle = QiblaCalculator.calculateNeedleAngle(smoothedAzimuth, currentBearing)

        val isAligned = needleAngle <= 3f || needleAngle >= 357f

        _uiState.update {
            it.copy(
                deviceAzimuth = smoothedAzimuth,
                needleAngle = needleAngle,
                isAlignedWithQibla = isAligned
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _uiState.update { it.copy(sensorAccuracy = accuracy) }
    }

    override fun onLocationChanged(location: Location) {
        processLocationUpdate(location)
    }

    override fun onCleared() {
        super.onCleared()
        stopSensors()
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}
    }
}
