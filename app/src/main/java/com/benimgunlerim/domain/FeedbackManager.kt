package com.benimgunlerim.domain

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Lightweight sound + haptic manager.
 * Sound files are optional — if a raw resource doesn't exist, it silently skips.
 */
@Singleton
class FeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val soundPool: SoundPool by lazy {
        SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
    }

    private val soundIds = mutableMapOf<String, Int>()

    private fun loadSound(name: String): Int? {
        soundIds[name]?.let { return it }
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId == 0) return null
        val id = soundPool.load(context, resId, 1)
        soundIds[name] = id
        return id
    }

    fun playSound(name: String, volume: Float = 0.7f) {
        val id = loadSound(name) ?: return
        soundPool.play(id, volume, volume, 1, 0, 1f)
    }

    // ── Haptic patterns ──────────────────────────────────────────────────────

    fun tapLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrateSafely(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }
    }

    fun tapMedium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrateSafely(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    fun tapHeavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrateSafely(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        }
    }

    fun doubleTap() {
        vibrateSafely(
            VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1),
        )
    }

    fun celebrationBurst() {
        vibrateSafely(
            VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30, 50, 60), -1),
        )
    }

    fun levelUpVibration() {
        vibrateSafely(
            VibrationEffect.createWaveform(
                longArrayOf(0, 50, 80, 50, 80, 50, 80, 100),
                intArrayOf(0, 100, 0, 150, 0, 200, 0, 255),
                -1,
            ),
        )
    }

    private fun vibrateSafely(effect: VibrationEffect) {
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            context.checkSelfPermission(Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        try {
            vibrator.vibrate(effect)
        } catch (_: SecurityException) {
            // Haptics are optional; permission/OEM failures must not crash completion.
        } catch (_: RuntimeException) {
            // Ignore vibrator service failures.
        }
    }
}
