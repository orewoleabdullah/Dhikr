package com.abdullah.dhikr.ui.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.SoundEffectConstants
import android.view.View

/**
 * Singleton helper managing persistent audio tone sound effects and haptic vibrations.
 */
object FeedbackManager {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    /**
     * Plays click sound if sound is enabled.
     */
    fun playClickSound(view: View?, isSoundEnabled: Boolean) {
        if (!isSoundEnabled) return
        try {
            view?.playSoundEffect(SoundEffectConstants.CLICK)
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
        } catch (_: Exception) {
            try {
                view?.playSoundEffect(SoundEffectConstants.CLICK)
            } catch (_: Exception) {}
        }
    }

    /**
     * Performs haptic vibration if vibration is enabled.
     */
    fun performHapticVibration(context: Context, isVibrationEnabled: Boolean, isCompletion: Boolean = false) {
        if (!isVibrationEnabled) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                if (isCompletion) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 100, 120), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 80, 100, 120), -1)
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(35)
                    }
                }
            }
        } catch (_: Exception) {}
    }
}
